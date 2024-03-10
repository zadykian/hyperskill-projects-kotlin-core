package indigo

data class Card(val suit: Char, val rank: String) {
    override fun toString(): String = "$rank$suit"
}

abstract class GetCardsResult private constructor() {
    class Success(val retrievedCards: List<Card>, val newDeck: Deck) : GetCardsResult()

    class Failure(val message: String) : GetCardsResult()
}

class Deck private constructor(private val cards: List<Card>) {
    constructor() : this(cards = orderedCards())

    fun shuffle(): Deck = Deck(this.cards.shuffled())

    fun getCards(numberOfCards: Int): GetCardsResult =
        when {
            numberOfCards !in 1..52 -> GetCardsResult.Failure(Messages.INVALID_NUMBER_OF_CARDS)
            numberOfCards > cards.size -> GetCardsResult.Failure(Messages.NOT_ENOUGH_CARDS)
            numberOfCards == cards.size -> GetCardsResult.Success(retrievedCards = cards, newDeck = emptyDeck())
            else -> {
                val retrievedCards = cards.subList(fromIndex = 0, toIndex = numberOfCards - 1)
                val leftCards = cards.subList(fromIndex = numberOfCards, toIndex = cards.lastIndex)
                GetCardsResult.Success(retrievedCards, newDeck = Deck(cards = leftCards))
            }
        }

    companion object {
        private val suits = listOf('♣', '♦', '♥', '♠')
        private val ranks = listOf("K", "Q", "J", "10", "9", "8", "7", "6", "5", "4", "3", "2", "A")

        fun emptyDeck() = Deck(emptyList())

        fun orderedCards() = suits.flatMap { suit -> ranks.map { rank -> Card(suit, rank) } }
    }
}