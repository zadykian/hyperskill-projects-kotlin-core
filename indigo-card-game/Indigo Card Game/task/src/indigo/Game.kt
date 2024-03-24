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
    private val io: IO
) {
    fun run(players: List<Player>) {
        io.write(Messages.GREETING)
        val firstPlayer = selectFirstPlayer(players)
        val (cardsOnTable, deck) = Deck().shuffled().getCards(numberOfCards = 4)
        io.write(Messages.initialCards(cardsOnTable))

        val (playersHands, initialDeck) = giveInitialCards(players, deck)

        val initialState = GameState(
            initialDeck,
            cardsOnTable,
            firstPlayer,
            playersHands
        )

        next(initialState)
    }

    private fun selectFirstPlayer(players: List<Player>): Player {
        io.write(Messages.PLAY_FIRST_REQUEST)
        return when (io.read().lowercase()) {
            Answers.YES -> players.single { it is User }
            Answers.NO -> players.single { it is Computer }
            else -> selectFirstPlayer(players)
        }
    }

    private fun giveInitialCards(players: List<Player>, deck: Deck): Pair<Map<Player, CardsInHand>, Deck> {
        var currentDeck = deck
        val playersWithCards = players.associateWith {
            val (cards, newDeck) = currentDeck.getCards(numberOfCards = 6)
            currentDeck = newDeck
            cards
        }
        return Pair(playersWithCards, currentDeck)
    }

    private tailrec fun next(state: GameState): Unit {
        io.write(Messages.onTable(state.cardsOnTable))

        if (state.cardsOnTable.size == Deck.allCards.size) {
            io.write(Messages.GAME_OVER)
            return
        }

        when (val current = state.currentPlayer) {
            is User -> {
                val hand = state.playersHands.getValue(current)
                io.write(Messages.inHand(hand))

            }

            is Computer -> {

            }
        }
    }

    private tailrec fun pickACard(cardsCount: Int): Int {
        io.write(Messages.chooseCardRequest(cardsCount))
        return when (val cardNumber = io.read().toIntOrNull()) {
            in 1..cardsCount -> cardNumber!!
            else -> pickACard(cardsCount)
        }
    }
}