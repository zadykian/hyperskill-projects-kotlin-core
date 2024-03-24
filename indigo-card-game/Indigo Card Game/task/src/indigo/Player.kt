package indigo

sealed interface Player {
    val name: String
        get() = this::class.simpleName!!

    fun chooseCard(cardsOnTable: CardsOnTable, cardsInHand: CardsInHand): Card
}

class User(private val io: IO) : Player {
    override fun chooseCard(cardsOnTable: CardsOnTable, cardsInHand: CardsInHand): Card {
        TODO("Not yet implemented")
    }
}

class Computer : Player {
    override fun chooseCard(cardsOnTable: CardsOnTable, cardsInHand: CardsInHand): Card {
        TODO("Not yet implemented")
    }
}