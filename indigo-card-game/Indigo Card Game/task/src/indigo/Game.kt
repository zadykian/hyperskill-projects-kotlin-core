package indigo

object Game {
    fun run(players: List<Player>, firstPlayerSelector: (List<Player>) -> Player?, io: IO) {
        io.write(Messages.GREETING)
        val firstPlayer = firstPlayerSelector(players)
        if (firstPlayer == null) {
            io.write(Messages.GAME_OVER)
            return
        }

        val statesSequence = generateSequence(seed = initialize(firstPlayer)) {
            beforeEach(io, previous = it)
            val nextState = makeProgress(it, players, firstPlayer)
            afterEach(io, previous = it, next = nextState)
            nextState
        }

        statesSequence.last()
    }

    private fun beforeEach(io: IO, previous: GameState) {
        val isBeforeFirstTurn =
            previous.playersState.isEmpty() && previous.cardsOnTable.size == Constants.INITIAL_CARDS_COUNT

        if (previous.cardWasPlayed || isBeforeFirstTurn) {
            io.write(Messages.TURN_SEPARATOR)
            io.write(Messages.cardsOnTable(previous.cardsOnTable))
        }
    }

    private fun afterEach(io: IO, previous: GameState, next: GameState?) {
        if (previous.isTerminal()) {
            return
        }

        if (previous.isInitial() && next != null) {
            io.write(Messages.initialCards(next.cardsOnTable))
        }

        val cardWinner =
            if (next != null && next.previousPlayerWon) previous.currentPlayer
            else null

        cardWinner?.let { io.write(Messages.playerWins(it)) }

        val isTerminalWithoutPreviousWinner = !previous.previousPlayerWon && next?.isTerminal() == true
        if (cardWinner != null || isTerminalWithoutPreviousWinner) {
            io.write(Messages.currentScore(next!!))
        }

        if (next == null || next.isTerminal()) {
            io.write(Messages.GAME_OVER)
        }
    }

    private fun initialize(firstPlayer: Player) =
        GameState(
            deck = Deck().shuffle(),
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
            else -> pickCard(state, allPlayers)
        }

    private fun placeCardsOnTable(state: GameState): GameState {
        require(state.isInitial()) { Errors.INVALID_GAME_STATE }
        val (initialCardsOnTable, newDeck) = state.deck.getCards(numberOfCards = Constants.INITIAL_CARDS_COUNT)
        return state.copy(deck = newDeck, cardsOnTable = initialCardsOnTable)
    }

    private fun dealCards(state: GameState, allPlayers: List<Player>): GameState {
        val cardsWithDecks = generateSequence(
            seed = Pair(emptyList<Card>(), state.deck)
        ) { it.second.getCards(numberOfCards = Constants.CARDS_PER_HAND_COUNT) }
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
            previousPlayerWon = false
        )
    }

    private fun pickCard(state: GameState, allPlayers: List<Player>): GameState? {
        val pickedCard = state.currentPlayer.chooseCard(
            cardsOnTable = state.cardsOnTable,
            cardsInHand = state.playersState.getValue(state.currentPlayer).cardsInHand
        )

        return pickedCard?.let {
            val playerWonCards = state.cardsOnTable.lastOrNull()?.run { rank == it.rank || suit == it.suit }
            return if (playerWonCards == true) onCardsWon(state, it, allPlayers)
            else onCardsLost(state, it, allPlayers)
        }
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
            lastCardWinner = state.currentPlayer,
            cardWasPlayed = true,
            previousPlayerWon = true
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
            previousPlayerWon = false
        )
    }

    private fun selectNextPlayer(current: Player, allPlayers: List<Player>): Player {
        val currentIndex = allPlayers.indexOf(current)
        require(currentIndex != -1) { Errors.UNKNOWN_CURRENT_PLAYER }
        val nextIndex = (currentIndex + 1) % allPlayers.size
        return allPlayers[nextIndex]
    }

    private fun terminate(state: GameState, firstPlayer: Player): GameState {
        require(state.handsAreEmpty() && state.deck.isEmpty()) {
            Errors.INVALID_GAME_STATE
        }

        if (state.cardsOnTable.isEmpty()) {
            return state.copy(
                playersState = assignBonusPoints(state.playersState, firstPlayer),
                cardWasPlayed = false
            )
        }

        val cardsFromTableOwner = state.lastCardWinner ?: firstPlayer

        val winnerState = state.playersState.getValue(cardsFromTableOwner).run {
            copy(
                score = score + state.cardsOnTable.sumOf { it.rank.points },
                wonCardsCount = wonCardsCount + state.cardsOnTable.size
            )
        }

        val newPlayersState = state.playersState
            .plus(cardsFromTableOwner to winnerState)
            .let { assignBonusPoints(it, firstPlayer) }

        return state.copy(
            cardsOnTable = emptyList(),
            playersState = newPlayersState,
            cardWasPlayed = false,
        )
    }

    private fun assignBonusPoints(
        playersState: Map<Player, PlayerState>,
        firstPlayer: Player
    ): Map<Player, PlayerState> {
        val mostCardsCount = playersState.maxOf { it.value.wonCardsCount }

        val byMostCards = playersState.asSequence()
            .singleOrNull { it.value.wonCardsCount == mostCardsCount }
            ?: playersState.entries.single { it.key == firstPlayer }

        val stateWithBonusPoints = byMostCards.value.let { it.copy(score = it.score + Constants.BONUS_POINTS) }
        return playersState.plus(byMostCards.key to stateWithBonusPoints)
    }
}
