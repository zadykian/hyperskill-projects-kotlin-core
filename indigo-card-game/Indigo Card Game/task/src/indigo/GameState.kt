package indigo

data class PlayerState(
    val cardsInHand: List<Card>,
    val score: Int = 0,
    val wonCardsCount: Int = 0,
)

class GameState private constructor(
    val parentEvent: GameEvent,
    val deck: Deck,
    val cardsOnTable: List<Card>,
    val playersState: Map<Player, PlayerState>,
    val currentPlayer: Player,
) {
    fun handsAreEmpty() = playersState.values.all { it.cardsInHand.isEmpty() }

    fun isInitial() = deck.isFull()

    fun isTerminal() = playersState.asSequence().sumOf { it.value.score } == Constants.totalPointsPerGame

    fun next(
        parentEvent: GameEvent,
        deck: Deck? = null,
        cardsOnTable: List<Card>? = null,
        playersState: Map<Player, PlayerState>? = null,
        currentPlayer: Player? = null,
    ) = GameState(
        parentEvent = parentEvent,
        deck ?: this.deck,
        cardsOnTable ?: this.cardsOnTable,
        playersState ?: this.playersState,
        currentPlayer ?: this.currentPlayer
    )

    fun parentEvents(): Sequence<GameEvent> = sequence {
        yield(parentEvent)
        when (parentEvent) {
            is GameCreated -> {}
            is GameProceeded -> yieldAll(parentEvent.previous.parentEvents())
            is GameTerminated -> yieldAll(parentEvent.previous.parentEvents())
        }
    }

    companion object {
        fun initial(
            deck: Deck,
            firstPlayer: Player
        ) = GameState(
            parentEvent = GameCreated,
            deck = deck,
            cardsOnTable = emptyList(),
            playersState = emptyMap(),
            currentPlayer = firstPlayer
        )
    }
}
