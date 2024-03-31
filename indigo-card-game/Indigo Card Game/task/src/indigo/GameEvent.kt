package indigo

sealed interface GameEvent

object GameCreated : GameEvent

sealed class GameProceeded private constructor(val previous: GameState) : GameEvent {
    class InitialCardsPlaced(previous: GameState) : GameProceeded(previous)

    class CardsDealt(previous: GameState) : GameProceeded(previous)

    sealed class CardPlayed(val pickedCard: Card, previous: GameState) : GameProceeded(previous) {
        val playedBy = previous.currentPlayer
    }

    class CardWon(pickedCard: Card, previous: GameState) : CardPlayed(pickedCard, previous)

    class CardLost(pickedCard: Card, previous: GameState) : CardPlayed(pickedCard, previous)
}

class GameCompleted(val previous: GameState) : GameEvent
