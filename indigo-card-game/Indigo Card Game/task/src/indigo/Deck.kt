package indigo

class Deck private constructor(private val cards: List<Card>) {
    constructor() : this(cards = allCards)

    fun shuffled(): Deck = Deck(this.cards.shuffled())

    fun getCards(numberOfCards: Int): Pair<List<Card>, Deck> {
        require(numberOfCards in 1..allCards.size) { Messages.INVALID_NUMBER_OF_CARDS }
        require(numberOfCards <= cards.size) { Messages.NOT_ENOUGH_CARDS }

        val retrievedCards = cards.subList(fromIndex = 0, toIndex = numberOfCards)
        val leftCards = cards.subList(fromIndex = numberOfCards, toIndex = cards.size)
        return Pair(retrievedCards, Deck(cards = leftCards))
    }

    companion object {
        val allCards: List<Card> by lazy {
            val suits = Suit.values()
            val ranks = Rank.values()
            suits.flatMap { suit -> ranks.asSequence().map { rank -> Card(suit, rank) } }
        }
    }
}
