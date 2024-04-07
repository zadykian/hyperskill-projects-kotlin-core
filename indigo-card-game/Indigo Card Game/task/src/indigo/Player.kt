package indigo

sealed class Player(name: String? = null) {
    val name: String = name ?: this::class.simpleName!!

    abstract val displayPlayedCards: Boolean

    abstract fun chooseCard(topCardOnTable: Card?, cardsInHand: List<Card>): Card?
}

class User(private val io: IO, name: String? = null) : Player(name) {
    override val displayPlayedCards: Boolean = false

    override fun chooseCard(topCardOnTable: Card?, cardsInHand: List<Card>): Card? {
        if (cardsInHand.isEmpty()) {
            return null
        }
        io.write(Messages.cardsInHand(cardsInHand))
        return pickCardNumber(cardsInHand.size)?.let { cardsInHand[it - 1] }
    }

    private tailrec fun pickCardNumber(cardsCount: Int): Int? {
        io.write(Messages.chooseCardRequest(cardsCount))
        val fromUser = io.read().lowercase()

        if (fromUser == Answers.EXIT) {
            return null
        }

        return when (val cardNumber = fromUser.toIntOrNull()) {
            in 1..cardsCount -> cardNumber!!
            else -> pickCardNumber(cardsCount)
        }
    }
}

class Computer(name: String? = null) : Player(name) {
    override val displayPlayedCards: Boolean = true

    override fun chooseCard(topCardOnTable: Card?, cardsInHand: List<Card>): Card? {
        if (cardsInHand.isEmpty()) {
            return null
        }

        val candidateCards =
            if (topCardOnTable == null) emptyList()
            else cardsInHand.filter { it.rank == topCardOnTable.rank || it.suit == topCardOnTable.suit }

        return when (candidateCards.size) {
            0 -> onEmptyCandidateCards(cardsInHand)
            1 -> candidateCards.single()
            else -> onMultipleCandidateCards(cardsInHand)
        }
    }

    private fun onEmptyCandidateCards(cardsInHand: List<Card>): Card {
        fun <T> tryGetGroupWithMaxEntries(selector: (Card) -> T) =
            cardsInHand
                .groupBy(selector)
                .asSequence()
                .filter { it.value.size > 1 }
                .sortedByDescending { it.value.size }
                .firstOrNull()?.value

        tryGetGroupWithMaxEntries { it.suit }?.let {
            return it.random()
        }

        tryGetGroupWithMaxEntries { it.rank }?.let {
            return it.random()
        }

        return cardsInHand.random()
    }

    private fun onMultipleCandidateCards(cardsInHand: List<Card>): Card {
        TODO()
    }
}
