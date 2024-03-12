package indigo

interface Player {
    fun chooseCard(cardsOnTable: CardsOnTable, cardsInHand: CardsInHand): Card
}

class User : Player {
    override fun chooseCard(cardsOnTable: CardsOnTable, cardsInHand: CardsInHand): Card {
        TODO("Not yet implemented")
    }
}

class Computer : Player {
    override fun chooseCard(cardsOnTable: CardsOnTable, cardsInHand: CardsInHand): Card {
        TODO("Not yet implemented")
    }
}