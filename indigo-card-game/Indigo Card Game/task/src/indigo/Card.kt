package indigo

enum class Rank(val symbol: String) {
    Ace("A"),
    Two("2"),
    Three("3"),
    Four("4"),
    Five("5"),
    Six("6"),
    Seven("7"),
    Eight("8"),
    Nine("9"),
    Ten("10"),
    Jack("J"),
    Queen("Q"),
    King("K"),
}

enum class Suit(val symbol: Char) {
    Clubs('♣'),
    Diamonds('♦'),
    Hearts('♥'),
    Spades('♠'),
}

data class Card(val suit: Suit, val rank: Rank) {
    override fun toString(): String = "${rank.symbol}${suit.symbol}"
}
