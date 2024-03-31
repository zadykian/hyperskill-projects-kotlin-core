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
    GameEvent(parentEvent = null)

class FirstPlayerSelected(val firstPlayer: Player, val allPlayers: List<Player>, parentEvent: GameCreated) :
    GameEvent(parentEvent)

class GameStarted(val initialState: GameState, parentEvent: FirstPlayerSelected) : GameEvent(parentEvent)

sealed class GameProceeded private constructor(
    val previousState: GameState, val nextState: GameState, parentEvent: GameEvent
) : GameEvent(parentEvent) {
    class InitialCardsPlaced(previousState: GameState, nextState: GameState, parentEvent: GameStarted) :
        GameProceeded(previousState, nextState, parentEvent)

    class CardsDealt(previousState: GameState, nextState: GameState, parentEvent: GameProceeded) :
        GameProceeded(previousState, nextState, parentEvent)

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
