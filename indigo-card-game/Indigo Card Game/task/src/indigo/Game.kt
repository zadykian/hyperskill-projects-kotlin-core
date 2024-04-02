package indigo

import indigo.GameProceeded.*

class Game(private val gameEventHandler: GameEventHandler) {
    fun run(players: List<Player>, firstPlayerSelector: (List<Player>) -> Player?) =
        generateSequence<GameEvent>(seed = GameCreated(players, firstPlayerSelector)) { onGameEvent(it) }
            .onEach { gameEventHandler.handle(event = it) }
            .last()
            .let {
                when (it) {
                    is GameCompleted -> it.finalState
                    is GameTerminated -> null
                    else -> throw Exception(Errors.INVALID_GAME_STATE)
                }
            }

    private fun onGameEvent(event: GameEvent): GameEvent? =
        when (event) {
            is GameCreated -> onGameCreated(event)
            is GameStarted -> onGameStarted(event)
            is GameProceeded -> onGameProceeded(event)
            is GameCompleted, is GameTerminated -> null
        }

    private fun onGameCreated(event: GameCreated): GameEvent {
        val firstPlayer = event.firstPlayerSelector(event.allPlayers) ?: return GameTerminated(parentEvent = event)
        val initialState = GameState.initial(Deck().shuffle(), firstPlayer, event.allPlayers)
        return GameStarted(initialState, parentEvent = event)
    }

    private fun onGameStarted(event: GameStarted): InitialCardsPlaced {
        val currentState = event.initialState
        val (initialCardsOnTable, newDeck) = currentState.deck.getCards(numberOfCards = Constants.INITIAL_CARDS_ON_TABLE_COUNT)
        val nextState = currentState.next(deck = newDeck, cardsOnTable = initialCardsOnTable)
        return InitialCardsPlaced(previousState = event.initialState, nextState = nextState, parentEvent = event)
    }

    private fun onGameProceeded(event: GameProceeded): GameEvent {
        val state = event.nextState

        if (state.run { handsAreEmpty() && deck.isEmpty() }) {
            val terminalState = complete(event)
            return GameCompleted(terminalState, parentEvent = event)
        }

        if (event.nextState.handsAreEmpty()) {
            val nextState = dealCards(event)
            return CardsDealt(previousState = state, nextState = nextState, parentEvent = event)
        }

        val pickedCard = state.currentPlayer.chooseCard(
            topCardOnTable = state.cardsOnTable.lastOrNull(),
            cardsInHand = state.playersState.getValue(state.currentPlayer).cardsInHand
        )

        if (pickedCard == null) {
            return GameTerminated(parentEvent = event)
        }

        val playerWonCards = state.cardsOnTable
            .lastOrNull()
            ?.run { rank == pickedCard.rank || suit == pickedCard.suit } ?: false

        val nextState = if (playerWonCards) assignCardsToWinner(state, pickedCard)
        else putLostCardOnTable(state, pickedCard)

        return CardPlayed(
            pickedCard = pickedCard,
            isWon = playerWonCards,
            previousState = state,
            nextState = nextState,
            parentEvent = event
        )
    }

    private fun dealCards(event: GameProceeded): GameState {
        val state = event.nextState

        val cardsWithDecks = generateSequence(
            seed = Pair(emptyList<Card>(), state.deck)
        ) { it.second.getCards(numberOfCards = Constants.CARDS_PER_HAND_COUNT) }
            .drop(1)
            .take(state.allPlayers.size)
            .toList()

        val dealtCards = cardsWithDecks.map { it.first }
        val newDeck = cardsWithDecks.last().second

        val newPlayersState = state.allPlayers
            .asSequence()
            .zip(dealtCards.asSequence())
            .associate {
                Pair(
                    it.first,
                    state.playersState[it.first]?.copy(cardsInHand = it.second) ?: PlayerState(cardsInHand = it.second)
                )
            }

        return state.next(deck = newDeck, playersState = newPlayersState)
    }

    private fun assignCardsToWinner(state: GameState, pickedCard: Card): GameState {
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
                cardsOnTable = emptyList(),
                playersState = playersState + (currentPlayer to newPlayerState),
                currentPlayer = selectNextPlayer(currentPlayer, allPlayers),
            )
        }
    }

    private fun putLostCardOnTable(
        state: GameState,
        pickedCard: Card,
    ): GameState {
        val oldPlayerState = state.playersState.getValue(state.currentPlayer)

        val newPlayerState = oldPlayerState.copy(
            cardsInHand = oldPlayerState.cardsInHand.minus(pickedCard),
        )

        return state.run {
            next(
                cardsOnTable = cardsOnTable + pickedCard,
                playersState = playersState + (currentPlayer to newPlayerState),
                currentPlayer = selectNextPlayer(currentPlayer, allPlayers),
            )
        }
    }

    private fun selectNextPlayer(current: Player, allPlayers: List<Player>): Player {
        val currentIndex = allPlayers.indexOf(current)
        require(currentIndex != -1) { Errors.UNKNOWN_PLAYER }
        val nextIndex = (currentIndex + 1) % allPlayers.size
        return allPlayers[nextIndex]
    }

    private fun complete(event: GameProceeded): GameState {
        val state = event.nextState
        require(state.handsAreEmpty() && state.deck.isEmpty()) { Errors.INVALID_GAME_STATE }

        if (state.cardsOnTable.isEmpty()) {
            return state.next(
                playersState = assignBonusPoints(state.playersState, state.firstPlayer),
            )
        }

        val lastCardWinner = event
            .parentEvents()
            .filterIsInstance<CardPlayed>()
            .firstOrNull { it.isWon }?.playedBy

        val cardsFromTableOwner = lastCardWinner ?: state.firstPlayer

        val winnerState = state.playersState.getValue(cardsFromTableOwner).run {
            copy(
                score = score + state.cardsOnTable.sumOf { it.rank.points },
                wonCardsCount = wonCardsCount + state.cardsOnTable.size
            )
        }

        val newPlayersState = state.playersState
            .plus(cardsFromTableOwner to winnerState)
            .let { assignBonusPoints(it, state.firstPlayer) }

        return state.next(cardsOnTable = emptyList(), playersState = newPlayersState)
    }

    private fun assignBonusPoints(
        playersState: Map<Player, PlayerState>,
        firstPlayer: Player
    ): Map<Player, PlayerState> {
        val mostCardsCount = playersState.maxOf { it.value.wonCardsCount }

        val byMostCards = playersState.asSequence()
            .singleOrNull { it.value.wonCardsCount == mostCardsCount }
            ?: playersState.entries.single { it.key == firstPlayer }

        val stateWithBonusPoints = byMostCards.value.run {
            copy(score = score + Constants.MOST_CARDS_BONUS_POINTS)
        }
        return playersState.plus(byMostCards.key to stateWithBonusPoints)
    }
}
