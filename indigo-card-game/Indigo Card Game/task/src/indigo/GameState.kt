package indigo

data class PlayerState(
    val cardsInHand: List<Card>,
    val score: Int = 0,
    val wonCardsCount: Int = 0,
) {
    init {
        require(cardsInHand.size <= Constants.CARDS_PER_HAND_COUNT) { Errors.INVALID_NUMBER_OF_CARDS }
    }
}

class GameState private constructor(
    val deck: Deck,
    val cardsOnTable: List<Card>,
    val playersState: Map<Player, PlayerState>,
    val currentPlayer: Player,
    val allPlayers: List<Player>,
    val firstPlayer: Player,
) {
    fun handsAreEmpty() = playersState.values.all { it.cardsInHand.isEmpty() }

    fun handsAreFull() = playersState.all { it.value.cardsInHand.size == Constants.CARDS_PER_HAND_COUNT }

    fun isInitial() = deck.isFull()

    fun isFinal() = playersState.asSequence().sumOf { it.value.score } == Constants.totalPointsPerGame

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
        firstPlayer = this.firstPlayer,
        allPlayers = this.allPlayers,
    )

    companion object {
        fun initial(
            deck: Deck,
            firstPlayer: Player,
            allPlayers: List<Player>,
        ): GameState {
            require(allPlayers.contains(firstPlayer)) { Errors.UNKNOWN_PLAYER }

            return GameState(
                deck = deck,
                cardsOnTable = emptyList(),
                playersState = emptyMap(),
                currentPlayer = firstPlayer,
                firstPlayer = firstPlayer,
                allPlayers = allPlayers,
            )
        }
    }
}
