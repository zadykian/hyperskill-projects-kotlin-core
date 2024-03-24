package indigo

object Messages {
    const val GREETING = "Indigo Card Game"
    const val PLAY_FIRST_REQUEST = "Play first?"


    const val GAME_OVER = "Game Over"

    const val CHOOSE_ACTION_REQUEST = "Choose an action (reset, shuffle, get, exit):"
    const val WRONG_ACTION = "Wrong action."
    const val DECK_IS_RESET = "Card deck is reset."
    const val DECK_IS_SHUFFLED = "Card deck is shuffled."
    const val NUM_OF_CARDS_REQUEST = "Number of cards:"
    const val INVALID_NUMBER_OF_CARDS = "Invalid number of cards."
    const val NOT_ENOUGH_CARDS = "The remaining cards are insufficient to meet the request."
    const val ON_EXIT = "Bye"

    fun initialCards(cards: Iterable<Card>) = "Initial cards on the table: ${cards.joinToString(separator = " ")}"

    fun onTable(cards: CardsOnTable) = "${cards.size} cards on the table, and the top card is ${cards.last()}"

    fun inHand(cards: CardsInHand) = "Cards in hand: ${cards.joinToString(separator = " ")}"

    fun chooseCardRequest(cardsCount: Int) = "Choose a card to play (1-$cardsCount):"
}

object Answers {
    const val YES = "yes"
    const val NO = "no"
}