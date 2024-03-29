package indigo

// todo

sealed interface GameEventKind

object InitialCardsPlaced : GameEventKind

object CardsAreDealt : GameEventKind

class CardIsPlayed(val playedBy: Player, val isWon: Boolean) : GameEventKind

class GameEvent(
    val previousState: GameState,
    val nextState: GameState,
    val eventKind: GameEventKind,
)
