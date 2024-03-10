package indigo

data class Card(val suit: Char, val rank: String) {
    override fun toString(): String = "$rank$suit"
}

object Deck {
    val suits = listOf('♦', '♥', '♠', '♣')
    val ranks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
    fun shuffled() : List<Card>
        = suits
            .flatMap { suit -> ranks.map { rank -> Card(suit, rank) } }
            .shuffled()
}

fun main() {
    fun <T> Iterable<T>.joinWithSpaces() = joinToString(separator = " ")
    println(Deck.ranks.joinWithSpaces())
    println(Deck.suits.joinWithSpaces())
    println(Deck.shuffled().joinWithSpaces())
}