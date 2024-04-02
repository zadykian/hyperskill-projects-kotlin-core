package indigo

object Constants {
    const val MIN_PLAYERS_COUNT = 2
    const val MAX_PLAYERS_COUNT = 8
    const val INITIAL_CARDS_ON_TABLE_COUNT = 4
    const val CARDS_PER_HAND_COUNT = 6
    const val MOST_CARDS_BONUS_POINTS = 3

    val totalPointsPerGame = Deck.allCards.sumOf { it.rank.points } + MOST_CARDS_BONUS_POINTS
}