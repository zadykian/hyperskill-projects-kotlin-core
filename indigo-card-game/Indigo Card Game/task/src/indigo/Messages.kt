package indigo

object Messages {
    const val GREETING = "Indigo Card Game"
    const val PLAY_FIRST_REQUEST = "Play first?"
    const val LINE_SEPARATOR = ""
    const val GAME_OVER = "Game Over"

    fun cards(cards: Iterable<Card>) = cards.joinToString(separator = " ")

    fun initialCards(cards: Iterable<Card>) = "Initial cards on the table: ${cards(cards)}"

    fun cardsOnTable(cards: List<Card>) =
        if (cards.isNotEmpty()) "${cards.size} cards on the table, and the top card is ${cards.last()}"
        else "No cards on the table"

    fun cardsInHand(cards: List<Card>): String {
        val asStringWithIndices = cards.withIndex().joinToString(separator = " ") { "${it.index + 1})${it.value}" }
        return "Cards in hand: $asStringWithIndices"
    }

    fun chooseCardRequest(cardsCount: Int) = "Choose a card to play (1-$cardsCount):"

    fun cardPlayed(player: Player, card: Card) = "${player.name} plays $card"

    fun playerWins(player: Player) = "${player.name} wins cards"

    fun currentScore(state: GameState): String {
        fun <T> joinPlayersStats(selector: (PlayerState) -> T) =
            state.playersState
                .asSequence()
                .joinToString(separator = " - ") { "${it.key.name} ${selector(it.value)}" }

        return """
            Score: ${joinPlayersStats { it.score }}
            Cards: ${joinPlayersStats { it.wonCardsCount }}
            """.trimIndent()
    }
}

object Answers {
    const val YES = "yes"
    const val NO = "no"
    const val EXIT = "exit"
}

object Errors {
    const val INVALID_NUMBER_OF_PLAYERS =
        "Invalid number of players provided! " +
                "[${Constants.MIN_PLAYERS_COUNT}..${Constants.MAX_PLAYERS_COUNT}] players are supported"

    const val INVALID_NUMBER_OF_CARDS = "Invalid number of cards!"
    const val NOT_ENOUGH_CARDS = "The remaining cards are insufficient to meet the request!"
    const val INVALID_GAME_STATE = "Invalid game state for current operation!"
    const val UNKNOWN_PLAYER = "Passed player does not belong to list of all players!"
}
