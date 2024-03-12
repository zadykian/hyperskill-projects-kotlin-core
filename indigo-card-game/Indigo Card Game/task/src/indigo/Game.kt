package indigo

typealias CardsOnTable = List<Card>
typealias CardsInHand = List<Card>

private class GameState(
    val deck: Deck,
    val cardsOnTable: CardsOnTable,
    val currentPlayer: Player,
    val playersHands: Map<Player, CardsInHand>
)

class Game(
    private val actionReceiver: ActionReceiver,
    private val outputWriter: (String) -> Unit
) {
    fun run(players: List<Player>) {
        outputWriter(Messages.GREETING)
        next(Deck())
    }

    private tailrec fun next(deck: Deck): Unit = when (val action = actionReceiver.next()) {
        is UserAction.Reset -> {
            val newDeck = Deck()
            outputWriter(Messages.DECK_IS_RESET)
            next(newDeck)
        }

        is UserAction.Shuffle -> {
            val newDeck = deck.shuffle()
            outputWriter(Messages.DECK_IS_SHUFFLED)
            next(newDeck)
        }

        is UserAction.GetCards -> when (val result = deck.getCards(action.numberOfCards)) {
            is GetCardsResult.Success -> {
                val cardsString = result.retrievedCards.joinToString(separator = " ")
                outputWriter(cardsString)
                next(result.newDeck)
            }

            is GetCardsResult.Failure -> {
                outputWriter(result.message)
                next(deck)
            }

            else -> throw IllegalArgumentException("Received unsupported result type: ${result::class}")
        }

        is UserAction.Exit -> outputWriter(Messages.ON_EXIT)

        else -> throw IllegalArgumentException("Received unsupported action type: ${action::class}")
    }
}