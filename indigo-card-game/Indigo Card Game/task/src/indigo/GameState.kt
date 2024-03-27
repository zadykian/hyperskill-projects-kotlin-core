package indigo

data class PlayerState(
    val cardsInHand: List<Card>,
    val score: Int = 0,
    val wonCardsCount: Int = 0
)

data class GameState(
    val deck: Deck,
    val cardsOnTable: List<Card>,
    val currentPlayer: Player,
    val playersState: Map<Player, PlayerState> = emptyMap(),
    val cardWasPlayed: Boolean,
    val previousPlayerWon: Boolean = false,
    val lastCardWinner: Player? = null,
) {
    fun handsAreEmpty() = playersState.values.all { it.cardsInHand.isEmpty() }

    fun isInitial() = deck.isFull()

    fun isTerminal() = handsAreEmpty()
            && deck.isEmpty()
            && cardsOnTable.isEmpty()
            && (playersState.asSequence().sumOf { it.value.score } == Constants.totalPointsPerGame)
}
