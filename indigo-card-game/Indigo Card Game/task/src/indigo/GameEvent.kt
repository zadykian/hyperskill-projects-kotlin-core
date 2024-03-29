package indigo

sealed interface GameEvent

object GameCreated : GameEvent

open class GameProceeded(val previous: GameState) : GameEvent

class InitialCardsPlaced(previous: GameState) : GameProceeded(previous)

class CardsDealt(previous: GameState) : GameProceeded(previous)

class CardWon(previous: GameState) : GameProceeded(previous) {
    val playedBy = previous.currentPlayer
}

class CardLost(previous: GameState) : GameProceeded(previous)

class GameTerminated(val previous: GameState) : GameEvent
