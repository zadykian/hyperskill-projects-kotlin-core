package indigo

typealias CardsOnTable = List<Card>
typealias CardsInHand = List<Card>

private class GameState(
    val deck: Deck,
    val cardsOnTable: CardsOnTable,
    val currentPlayer: Player,
    val playersHands: Map<Player, CardsInHand>
)

class Game(private val io: IO) {
    fun run(players: List<Player>, firstPlayerSelector: (List<Player>) -> Player?) {
        io.write(Messages.GREETING)
        val firstPlayer = firstPlayerSelector(players)
        if (firstPlayer == null) {
            io.write(Messages.GAME_OVER)
            return
        }

        val (cardsOnTable, deck) = Deck().shuffled().getCards(numberOfCards = Constants.INIT_CARDS_COUNT)
        io.write(Messages.initialCards(cardsOnTable))
        val (playersHands, initialDeck) = giveCards(players, deck)

        val initialState = GameState(
            initialDeck,
            cardsOnTable,
            firstPlayer,
            playersHands
        )

        next(initialState, players)
    }

    private tailrec fun next(state: GameState, allPlayers: List<Player>) {
        io.write("")
        io.write(Messages.onTable(state.cardsOnTable))

        if (state.cardsOnTable.size == Deck.allCards.size) {
            io.write(Messages.GAME_OVER)
            return
        }

        val (currentHands, currentDeck) =
            if (state.playersHands.values.all { it.isEmpty() }) giveCards(allPlayers, state.deck)
            else Pair(state.playersHands, state.deck)

        val currentPlayer = state.currentPlayer
        val hand = currentHands.getValue(currentPlayer)
        val pickedCard = currentPlayer.chooseCard(state.cardsOnTable, hand)

        if (pickedCard == null) {
            io.write(Messages.GAME_OVER)
            return
        }

        val nextState = GameState(
            deck = currentDeck,
            cardsOnTable = state.cardsOnTable + pickedCard,
            playersHands = currentHands + (currentPlayer to hand.minus(pickedCard)),
            currentPlayer = selectNextPlayer(currentPlayer, allPlayers)
        )

        next(nextState, allPlayers)
    }

    private fun giveCards(players: List<Player>, deck: Deck): Pair<Map<Player, CardsInHand>, Deck> {
        var currentDeck = deck
        val playersWithCards = players.associateWith {
            val (cards, newDeck) = currentDeck.getCards(numberOfCards = Constants.CARDS_PER_HAND)
            currentDeck = newDeck
            cards
        }
        return Pair(playersWithCards, currentDeck)
    }

    private fun selectNextPlayer(current: Player, allPlayers: List<Player>): Player {
        val currentIndex = allPlayers.indexOf(current)
        require(currentIndex != -1) { "List 'allPlayers' is expected to contain 'current'!" }
        val nextIndex = (currentIndex + 1) % allPlayers.size
        return allPlayers[nextIndex]
    }
}

private object Constants {
    const val INIT_CARDS_COUNT = 4
    const val CARDS_PER_HAND = 6
}
