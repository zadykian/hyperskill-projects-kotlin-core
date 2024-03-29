package indigo

object Game {
    fun run(players: List<Player>, firstPlayerSelector: (List<Player>) -> Player?, io: IO) {
        io.write(Messages.GREETING)
        val firstPlayer = firstPlayerSelector(players)
        if (firstPlayer == null) {
            io.write(Messages.GAME_OVER)
            return
        }

        val initial = GameState.initial(Deck().shuffle(), firstPlayer)

        val statesSequence = generateSequence(seed = initial) {
            beforeEach(io, previousState = it)
            val nextState = makeProgress(it, players, firstPlayer)
            afterEach(io, previousState = it, nextState = nextState)
            nextState
        }

        statesSequence.last()
    }

    private fun beforeEach(io: IO, previousState: GameState) {
        if (previousState.parentEvent.let { it is CardWon || it is CardLost }) {
            io.write(Messages.LINE_SEPARATOR)
            io.write(Messages.cardsOnTable(previousState.cardsOnTable))
        }
    }

    private fun afterEach(io: IO, previousState: GameState, nextState: GameState?) {
        if (previousState.parentEvent is GameTerminated) {
            return
        }

        if (nextState?.parentEvent is InitialCardsPlaced) {
            io.write(Messages.initialCards(nextState.cardsOnTable))
            io.write(Messages.LINE_SEPARATOR)
            io.write(Messages.cardsOnTable(previousState.cardsOnTable))
        }

        val cardWinner = nextState?.parentEvent?.let {
            if (it is CardWon) it.playedBy
            else null
        }

        cardWinner?.let { io.write(Messages.playerWins(it)) }

        if (cardWinner != null
            || (nextState?.isTerminal() == true && previousState.parentEvent !is CardWon)
        ) {
            io.write(Messages.currentScore(nextState))
        }

        if (nextState == null || nextState.isTerminal()) {
            io.write(Messages.GAME_OVER)
        }
    }

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
        val event = InitialCardsPlaced(previous = state)
        return state.next(parentEvent = event, deck = newDeck, cardsOnTable = initialCardsOnTable)
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

        return state.next(
            parentEvent = CardsDealt(previous = state),
            deck = newDeck,
            playersState = newPlayersState
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
        val oldPlayerState = state.playersState.getValue(state.currentPlayer)
        val wonCards = state.cardsOnTable + pickedCard

        val newPlayerState = oldPlayerState.run {
            copy(
                cardsInHand = cardsInHand.minus(pickedCard),
                score = score + wonCards.sumOf { it.rank.points },
                wonCardsCount = wonCardsCount + wonCards.size,
            )
        }

        return state.run {
            next(
                parentEvent = CardWon(previous = this),
                cardsOnTable = emptyList(),
                playersState = playersState + (currentPlayer to newPlayerState),
                currentPlayer = selectNextPlayer(currentPlayer, allPlayers),
            )
        }
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

        return state.run {
            next(
                parentEvent = CardLost(previous = this),
                cardsOnTable = cardsOnTable + pickedCard,
                playersState = playersState + (currentPlayer to newPlayerState),
                currentPlayer = selectNextPlayer(currentPlayer, allPlayers),
            )
        }
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
            return state.next(
                parentEvent = GameTerminated(previous = state),
                playersState = assignBonusPoints(state.playersState, firstPlayer),
            )
        }

        val cardsFromTableOwner = state
            .parentEvents()
            .filterIsInstance<CardWon>()
            .firstOrNull()?.playedBy ?: firstPlayer

        val winnerState = state.playersState.getValue(cardsFromTableOwner).run {
            copy(
                score = score + state.cardsOnTable.sumOf { it.rank.points },
                wonCardsCount = wonCardsCount + state.cardsOnTable.size
            )
        }

        val newPlayersState = state.playersState
            .plus(cardsFromTableOwner to winnerState)
            .let { assignBonusPoints(it, firstPlayer) }

        return state.next(
            parentEvent = GameTerminated(previous = state),
            cardsOnTable = emptyList(),
            playersState = newPlayersState,
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
