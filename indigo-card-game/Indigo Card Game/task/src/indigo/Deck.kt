package indigo

enum class Rank(val symbol: String, val points: Int = 0) {
    Ace("A", points = 1),
    Two("2"),
    Three("3"),
    Four("4"),
    Five("5"),
    Six("6"),
    Seven("7"),
    Eight("8"),
    Nine("9"),
    Ten("10", points = 1),
    Jack("J", points = 1),
    Queen("Q", points = 1),
    King("K", points = 1),
}

enum class Suit(val symbol: Char) {
    Clubs('♣'),
    Diamonds('♦'),
    Hearts('♥'),
    Spades('♠'),
}

class Card(val suit: Suit, val rank: Rank) {
    override fun toString(): String = "${rank.symbol}${suit.symbol}"
}

class Deck private constructor(private val cards: List<Card>) {
    constructor() : this(cards = allCards)

    fun isEmpty() = cards.isEmpty()

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
