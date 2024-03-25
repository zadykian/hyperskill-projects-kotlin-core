package indigo

object Messages {
    const val GREETING = "Indigo Card Game"
    const val PLAY_FIRST_REQUEST = "Play first?"
    const val GAME_OVER = "Game Over"

    const val INVALID_NUMBER_OF_CARDS = "Invalid number of cards."
    const val NOT_ENOUGH_CARDS = "The remaining cards are insufficient to meet the request."

    fun initialCards(cards: Iterable<Card>) = "Initial cards on the table: ${cards.joinToString(separator = " ")}"

    fun onTable(cards: CardsOnTable) = "${cards.size} cards on the table, and the top card is ${cards.last()}"

    fun inHand(cards: CardsInHand): String {
        val asStringWithIndices = cards.withIndex().joinToString(separator = " ") { "${it.index + 1})${it.value}" }
        return "Cards in hand: $asStringWithIndices"
    }

    fun chooseCardRequest(cardsCount: Int) = "Choose a card to play (1-$cardsCount):"

    fun cardPlayed(player: Player, card: Card) = "${player.name} plays $card"
}

object Answers {
    const val YES = "yes"
    const val NO = "no"
    const val EXIT = "exit"
}