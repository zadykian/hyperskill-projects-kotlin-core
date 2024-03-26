package indigo

class Game(private val io: IO) {
    fun run(players: List<Player>, firstPlayerSelector: (List<Player>) -> Player?) {
        io.write(Messages.GREETING)
        val firstPlayer = firstPlayerSelector(players)
        if (firstPlayer == null) {
            io.write(Messages.GAME_OVER)
            return
        }

        val initialState = initialize(players, firstPlayer)
        nextTurn(initialState, players, firstPlayer)
    }

    private fun initialize(allPlayers: List<Player>, firstPlayer: Player): GameState {
        val (cardsOnTable, deck) = Deck().shuffled().getCards(numberOfCards = INIT_CARDS_COUNT)
        io.write(Messages.initialCards(cardsOnTable))
        val (cardsPerPlayer, initialDeck) = dealCards(deck, allPlayers.size)

        val playersState = allPlayers
            .zip(cardsPerPlayer)
            .associate { pair -> Pair(pair.first, PlayerState(pair.second)) }

        return GameState(initialDeck, cardsOnTable, firstPlayer, playersState)
    }

    private tailrec fun nextTurn(state: GameState, allPlayers: List<Player>, firstPlayer: Player) {
        io.write(Messages.TURN_SEPARATOR)
        io.write(Messages.cardsOnTable(state.cardsOnTable))

        if (state.handsAreEmpty() && state.deck.isEmpty()) {
            terminate(state, firstPlayer)
            return
        }

        if (state.handsAreEmpty()) {
            val newState = StateTransformers.onEmptyHands(state)
            nextTurn(newState, allPlayers, firstPlayer)
            return
        }

        val pickedCard = state.currentPlayer.chooseCard(
            cardsOnTable = state.cardsOnTable,
            cardsInHand = state.playersState.getValue(state.currentPlayer).cardsInHand
        )

        if (pickedCard == null) {
            io.write(Messages.GAME_OVER)
            return
        }

        val playerWonCards = state.cardsOnTable
            .lastOrNull()
            ?.run { rank == pickedCard.rank || suit == pickedCard.suit } ?: false

        val nextState = if (playerWonCards) {
            io.write(Messages.playerWins(state.currentPlayer))
            io.write(Messages.currentScore(state))
            StateTransformers.onCardsWon(state, pickedCard, allPlayers)
        } else {
            StateTransformers.onCardsLost(state, pickedCard, allPlayers)
        }

        nextTurn(nextState, allPlayers, firstPlayer)
    }

    private fun terminate(state: GameState, firstPlayer: Player) {
        fun writeGameSummary(finalState: GameState) {
            io.write(Messages.currentScore(finalState))
            io.write(Messages.GAME_OVER)
        }

        if (state.cardsOnTable.isEmpty()) {
            writeGameSummary(state)
            return
        }

        val cardsFromTableOwner = state.lastCardWinner ?: firstPlayer

        val winnerState = state.playersState.getValue(cardsFromTableOwner).run {
            copy(
                score = score + state.cardsOnTable.sumOf { it.rank.points },
                wonCardsCount = wonCardsCount + state.cardsOnTable.size
            )
        }

        val finalState = state.copy(
            cardsOnTable = emptyList(),
            playersState = state.playersState + (cardsFromTableOwner to winnerState)
        )

        writeGameSummary(finalState)
    }

    private object StateTransformers {
        fun onEmptyHands(state: GameState): GameState {
            val (dealtCards, newDeck) = dealCards(state.deck, state.playersState.size)

            val newPlayersState = state
                .playersState
                .asSequence()
                .zip(dealtCards.asSequence())
                .map { Pair(it.first.key, it.first.value.copy(cardsInHand = it.second)) }
                .toMap()

            return state.copy(deck = newDeck, playersState = newPlayersState)
        }

        fun onCardsWon(
            state: GameState,
            pickedCard: Card,
            allPlayers: List<Player>
        ): GameState {
            val playerState = state.playersState.getValue(state.currentPlayer)
            val wonCards = state.cardsOnTable + pickedCard

            val newPlayerState = playerState.copy(
                cardsInHand = playerState.cardsInHand.minus(pickedCard),
                score = playerState.score + wonCards.sumOf { it.rank.points },
                wonCardsCount = playerState.wonCardsCount + wonCards.size
            )

            return state.copy(
                cardsOnTable = emptyList(),
                playersState = state.playersState + (state.currentPlayer to newPlayerState),
                currentPlayer = selectNextPlayer(state.currentPlayer, allPlayers)
            )
        }

        fun onCardsLost(
            state: GameState,
            pickedCard: Card,
            allPlayers: List<Player>
        ): GameState {
            val oldPlayerState = state.playersState.getValue(state.currentPlayer)

            val newPlayerState = oldPlayerState.copy(
                cardsInHand = oldPlayerState.cardsInHand.minus(pickedCard),
            )

            return state.copy(
                cardsOnTable = state.cardsOnTable + pickedCard,
                playersState = state.playersState + (state.currentPlayer to newPlayerState),
                currentPlayer = selectNextPlayer(state.currentPlayer, allPlayers)
            )
        }
    }

    companion object {
        const val INIT_CARDS_COUNT = 4
        private const val CARDS_PER_HAND = 6

        private fun dealCards(deck: Deck, playersCount: Int): Pair<List<List<Card>>, Deck> {
            val cardsWithDecks = generateSequence(
                seed = Pair(emptyList<Card>(), deck)
            ) { it.second.getCards(numberOfCards = CARDS_PER_HAND) }
                .drop(1)
                .take(playersCount)
                .toList()

            return Pair(
                cardsWithDecks.map { it.first },
                cardsWithDecks.last().second
            )
        }

        private fun selectNextPlayer(current: Player, allPlayers: List<Player>): Player {
            val currentIndex = allPlayers.indexOf(current)
            require(currentIndex != -1) { "List 'allPlayers' is expected to contain 'current'!" }
            val nextIndex = (currentIndex + 1) % allPlayers.size
            return allPlayers[nextIndex]
        }
    }
}
