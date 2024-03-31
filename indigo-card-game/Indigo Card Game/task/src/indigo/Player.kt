package indigo

sealed class Player(name: String? = null) {
    val name: String = name ?: this::class.simpleName!!

    abstract val displayPlayedCards: Boolean

    abstract fun chooseCard(topCardOnTable: Card?, cardsInHand: List<Card>): Card?
}

class User(private val io: IO, name: String? = null) : Player(name) {
    override val displayPlayedCards: Boolean = false

    override fun chooseCard(topCardOnTable: Card?, cardsInHand: List<Card>): Card? {
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

    override fun chooseCard(topCardOnTable: Card?, cardsInHand: List<Card>): Card {
        val pickedCard = cardsInHand.first()
        return pickedCard
    }
}
