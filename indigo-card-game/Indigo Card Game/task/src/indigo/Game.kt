package indigo

object Game {
    private const val INITIAL_CARDS_COUNT = 4
    private const val CARDS_PER_HAND_COUNT = 6

    fun run(players: List<Player>, firstPlayerSelector: (List<Player>) -> Player?, io: IO) {
        io.write(Messages.GREETING)
        val firstPlayer = firstPlayerSelector(players)
        if (firstPlayer == null) {
            io.write(Messages.GAME_OVER)
            return
        }

        fun beforeEach(previous: GameState) {
            if (!previous.isInitial() && !previous.cardWasPlayed) {
                return
            }

            io.write(Messages.TURN_SEPARATOR)
            io.write(Messages.cardsOnTable(previous.cardsOnTable))
        }

        fun afterEach(previous: GameState, next: GameState?) {
            val winner =
                if (previous.lastCardWinner != next?.lastCardWinner) next?.lastCardWinner
                else null

            winner?.let { io.write(Messages.playerWins(it)) }

            if (winner != null || next?.isTerminal() == true) {
                io.write(Messages.currentScore(next!!))
            }

            if (next == null || next.isTerminal()) {
                io.write(Messages.GAME_OVER)
            }
        }

        val statesSequence = generateSequence(seed = initialize(firstPlayer)) {
            beforeEach(previous = it)
            val nextState = makeProgress(it, players, firstPlayer)
            afterEach(previous = it, next = nextState)
            nextState
        }

        statesSequence.last()
    }

    private fun initialize(firstPlayer: Player) =
        GameState(
            deck = Deck().shuffled(),
            cardsOnTable = emptyList(),
            currentPlayer = firstPlayer,
            cardWasPlayed = false
        )

    private fun makeProgress(state: GameState, allPlayers: List<Player>, firstPlayer: Player) =
        when {
            state.isInitial() -> placeCardsOnTable(state)
            state.isTerminal() -> null
            state.handsAreEmpty() && state.deck.isEmpty() -> terminate(state, firstPlayer)
            state.handsAreEmpty() -> dealCards(state, allPlayers)
            else -> {
                val pickedCard = state.currentPlayer.chooseCard(
                    cardsOnTable = state.cardsOnTable,
                    cardsInHand = state.playersState.getValue(state.currentPlayer).cardsInHand
                )

                pickedCard?.let {
                    val playerWonCards = state.cardsOnTable.lastOrNull()?.run { rank == it.rank || suit == it.suit }
                    return if (playerWonCards == true) onCardsWon(state, it, allPlayers)
                    else onCardsLost(state, it, allPlayers)
                }
            }
        }

    private fun placeCardsOnTable(state: GameState): GameState {
        require(state.isInitial()) { Errors.INVALID_GAME_STATE }
        val (initialCardsOnTable, newDeck) = state.deck.getCards(numberOfCards = INITIAL_CARDS_COUNT)
        return state.copy(deck = newDeck, cardsOnTable = initialCardsOnTable)
    }

    private fun dealCards(state: GameState, allPlayers: List<Player>): GameState {
        val cardsWithDecks = generateSequence(
            seed = Pair(emptyList<Card>(), state.deck)
        ) { it.second.getCards(numberOfCards = CARDS_PER_HAND_COUNT) }
            .drop(1)
            .take(allPlayers.size)
            .toList()

        val dealtCards = cardsWithDecks.map { it.first }
        val newDeck = cardsWithDecks.last().second

        val newPlayersState = allPlayers
            .asSequence()
            .zip(dealtCards.asSequence())
            .associate {
                Pair(
                    it.first,
                    state.playersState[it.first]?.copy(cardsInHand = it.second) ?: PlayerState(cardsInHand = it.second)
                )
            }

        return state.copy(
            deck = newDeck,
            playersState = newPlayersState,
            cardWasPlayed = false,
        )
    }

    private fun onCardsWon(
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
            currentPlayer = selectNextPlayer(state.currentPlayer, allPlayers),
            cardWasPlayed = true,
        )
    }

    private fun onCardsLost(
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
            currentPlayer = selectNextPlayer(state.currentPlayer, allPlayers),
            cardWasPlayed = true,
        )
    }

    private fun terminate(state: GameState, firstPlayer: Player): GameState {
        require(state.handsAreEmpty() && state.deck.isEmpty()) {
            Errors.INVALID_GAME_STATE
        }

        if (state.cardsOnTable.isEmpty()) {
            return state
        }

        val cardsFromTableOwner = state.lastCardWinner ?: firstPlayer

        val winnerState = state.playersState.getValue(cardsFromTableOwner).run {
            copy(
                score = score + state.cardsOnTable.sumOf { it.rank.points },
                wonCardsCount = wonCardsCount + state.cardsOnTable.size
            )
        }

        return state.copy(
            cardsOnTable = emptyList(),
            playersState = state.playersState + (cardsFromTableOwner to winnerState),
            cardWasPlayed = false
        )
    }

    private fun selectNextPlayer(current: Player, allPlayers: List<Player>): Player {
        val currentIndex = allPlayers.indexOf(current)
        require(currentIndex != -1) { Errors.UNKNOWN_CURRENT_PLAYER }
        val nextIndex = (currentIndex + 1) % allPlayers.size
        return allPlayers[nextIndex]
    }
}
