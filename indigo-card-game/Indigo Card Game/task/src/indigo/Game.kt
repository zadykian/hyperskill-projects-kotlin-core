package indigo

private class Turn(val from: GameState, val to: GameState) {
    init {
        require(from.currentPlayer != to.currentPlayer) { Errors.CURRENT_PLAYER_IS_NOT_SWITCHED }
    }

    val winner: Player? =
        if (from.lastCardWinner != to.lastCardWinner) to.lastCardWinner
        else null

    val isFinal: Boolean = to.isTerminal()
}

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

        fun beforeEachTurn(turn: Turn) {
            io.write(Messages.TURN_SEPARATOR)
            io.write(Messages.cardsOnTable(turn.from.cardsOnTable))
        }

        fun afterEachTurn(turn: Turn) {
            turn.winner?.let { io.write(Messages.playerWins(it)) }

            if (turn.winner != null || turn.isFinal) {
                io.write(Messages.currentScore(turn.to))
            }

            if (turn.isFinal) {
                io.write(Messages.GAME_OVER)
            }
        }

        val statesSequence = generateSequence(seed = initialize(firstPlayer, io)) {
            makeProgress(it, players, firstPlayer)
        }

        val res = sequence<Turn> {

        }

        statesSequence
            .zipWithNext { from, to -> Turn(from, to) }
            .onEach { beforeEachTurn(it) }
            .forEach { afterEachTurn(it) }
    }

    private fun initialize(firstPlayer: Player, io: IO): GameState {
        val (cardsOnTable, deck) = Deck().shuffled().getCards(numberOfCards = INITIAL_CARDS_COUNT)
        io.write(Messages.initialCards(cardsOnTable))
        return GameState(deck, cardsOnTable, firstPlayer)
    }

    private fun makeProgress(state: GameState, allPlayers: List<Player>, firstPlayer: Player): GameState? {
        if (state.isTerminal()) {
            return null
        }

        if (state.handsAreEmpty() && state.deck.isEmpty()) {
            return terminate(state, firstPlayer)
        }

        if (state.handsAreEmpty()) {
            return dealCards(state, allPlayers)
        }

        val pickedCard = state.currentPlayer.chooseCard(
            cardsOnTable = state.cardsOnTable,
            cardsInHand = state.playersState.getValue(state.currentPlayer).cardsInHand
        )

        return pickedCard?.let {
            val playerWonCards = state.cardsOnTable.lastOrNull()?.run { rank == it.rank || suit == it.suit } ?: false
            return if (playerWonCards) onCardsWon(state, it, allPlayers)
            else onCardsLost(state, it, allPlayers)
        }
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

        val newPlayersState = state
            .playersState
            .asSequence()
            .zip(dealtCards.asSequence())
            .map { Pair(it.first.key, it.first.value.copy(cardsInHand = it.second)) }
            .toMap()

        return state.copy(deck = newDeck, playersState = newPlayersState)
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
            currentPlayer = selectNextPlayer(state.currentPlayer, allPlayers)
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
            currentPlayer = selectNextPlayer(state.currentPlayer, allPlayers)
        )
    }

    private fun terminate(state: GameState, firstPlayer: Player): GameState {
        require(state.handsAreEmpty() && state.deck.isEmpty()) {
            Errors.UNABLE_TO_TERMINATE
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
            playersState = state.playersState + (cardsFromTableOwner to winnerState)
        )
    }

    private fun selectNextPlayer(current: Player, allPlayers: List<Player>): Player {
        val currentIndex = allPlayers.indexOf(current)
        require(currentIndex != -1) { Errors.UNKNOWN_CURRENT_PLAYER }
        val nextIndex = (currentIndex + 1) % allPlayers.size
        return allPlayers[nextIndex]
    }
}
