package indigo

class TurnHistoryEntry(val sourceStateNumber: Int, val wasWon: Boolean)

data class PlayerState(
    val cardsInHand: List<Card>,
    val score: Int = 0,
    val wonCardsCount: Int = 0,
    val turnHistory: List<TurnHistoryEntry> = emptyList()
)

class GameState private constructor(
    val deck: Deck,
    val cardsOnTable: List<Card>,
    val playersState: Map<Player, PlayerState>,
    val currentPlayer: Player,
    val number: Int,
) {
    fun handsAreEmpty() = playersState.values.all { it.cardsInHand.isEmpty() }

    fun isInitial() = deck.isFull()

    fun isTerminal() = playersState.asSequence().sumOf { it.value.score } == Constants.totalPointsPerGame

    fun next(
        deck: Deck? = null,
        cardsOnTable: List<Card>? = null,
        playersState: Map<Player, PlayerState>? = null,
        currentPlayer: Player? = null,
    ) = GameState(
        deck ?: this.deck,
        cardsOnTable ?: this.cardsOnTable,
        playersState ?: this.playersState,
        currentPlayer ?: this.currentPlayer,
        number = this.number.inc()
    )

    companion object {
        fun initial(
            deck: Deck,
            firstPlayer: Player
        ) = GameState(
            deck = deck,
            cardsOnTable = emptyList(),
            playersState = emptyMap(),
            currentPlayer = firstPlayer,
            number = 0
        )
    }
}
