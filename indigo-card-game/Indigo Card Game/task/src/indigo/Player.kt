package indigo

sealed interface Player {
    val name: String
        get() = this::class.simpleName!!

    fun chooseCard(cardsOnTable: CardsOnTable, cardsInHand: CardsInHand): Card?
}

class User(private val io: IO) : Player {
    override fun chooseCard(cardsOnTable: CardsOnTable, cardsInHand: CardsInHand): Card? {
        io.write(Messages.inHand(cardsInHand))
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

class Computer(private val io: IO) : Player {
    override fun chooseCard(cardsOnTable: CardsOnTable, cardsInHand: CardsInHand): Card {
        val pickedCard = cardsInHand.first()
        io.write(Messages.cardPlayed(this, pickedCard))
        return pickedCard
    }
}