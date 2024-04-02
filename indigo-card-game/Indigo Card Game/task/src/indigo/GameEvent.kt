package indigo

sealed class GameEvent(private val parentEvent: GameEvent?) {
    fun parentEvents(): Sequence<GameEvent> = sequence {
        if (parentEvent == null) {
            return@sequence
        }
        yield(parentEvent)
        yieldAll(parentEvent.parentEvents())
    }
}

class GameCreated(val allPlayers: List<Player>, val firstPlayerSelector: (List<Player>) -> Player?) :
    GameEvent(parentEvent = null) {
    init {
        require(allPlayers.size in Constants.MIN_PLAYERS_COUNT..Constants.MAX_PLAYERS_COUNT) {
            Errors.INVALID_NUMBER_OF_PLAYERS
        }
    }
}

class GameStarted(val initialState: GameState, parentEvent: GameCreated) : GameEvent(parentEvent) {
    init {
        require(initialState.isInitial()) { Errors.INVALID_GAME_STATE }
    }
}

sealed class GameProceeded private constructor(
    val previousState: GameState, val nextState: GameState, parentEvent: GameEvent
) : GameEvent(parentEvent) {
    class InitialCardsPlaced(previousState: GameState, nextState: GameState, parentEvent: GameStarted) :
        GameProceeded(previousState, nextState, parentEvent) {
        init {
            require(nextState.cardsOnTable.size == Constants.INITIAL_CARDS_ON_TABLE_COUNT) {
                Errors.INVALID_GAME_STATE
            }
        }
    }

    class CardsDealt(previousState: GameState, nextState: GameState, parentEvent: GameProceeded) :
        GameProceeded(previousState, nextState, parentEvent) {
        init {
            require(nextState.handsAreFull()) { Errors.INVALID_GAME_STATE }
        }
    }

    class CardPlayed(
        val pickedCard: Card,
        val isWon: Boolean,
        previousState: GameState,
        nextState: GameState,
        parentEvent: GameProceeded
    ) :
        GameProceeded(previousState, nextState, parentEvent) {
        val playedBy = previousState.currentPlayer
    }
}

class GameCompleted(val finalState: GameState, parentEvent: GameProceeded) : GameEvent(parentEvent) {
    init {
        require(finalState.isFinal()) { Errors.INVALID_GAME_STATE }
    }
}

class GameTerminated(parentEvent: GameEvent) : GameEvent(parentEvent)
