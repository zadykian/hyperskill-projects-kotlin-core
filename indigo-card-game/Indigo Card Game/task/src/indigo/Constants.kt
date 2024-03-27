package indigo

object Constants {
    const val INITIAL_CARDS_COUNT = 4
    const val CARDS_PER_HAND_COUNT = 6
    const val BONUS_POINTS = 3

    val totalPointsPerGame = Deck.allCards.sumOf { it.rank.points } + BONUS_POINTS
}