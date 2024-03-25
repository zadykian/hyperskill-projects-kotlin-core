package indigo

data class PlayerState(
    val cardsInHand: List<Card>,
    val score: Int = 0,
    val wonCardsCount: Int = 0
)

private class GameState(
    val deck: Deck,
    val cardsOnTable: List<Card>,
    val currentPlayer: Player,
    val playersState: Map<Player, PlayerState>,
    val lastCardWinner: Player? = null
) {
    fun handsAreEmpty() = playersState.values.all { it.cardsInHand.isEmpty() }
}

class Game(private val io: IO) {
    fun run(players: List<Player>, firstPlayerSelector: (List<Player>) -> Player?) {
        io.write(Messages.GREETING)
        val firstPlayer = firstPlayerSelector(players)
        if (firstPlayer == null) {
            io.write(Messages.GAME_OVER)
            return
        }

        val (cardsOnTable, deck) = Deck().shuffled().getCards(numberOfCards = INIT_CARDS_COUNT)
        io.write(Messages.initialCards(cardsOnTable))
        val (cardsPerPlayer, initialDeck) = dealCards(deck, players.size)

        val initialState = GameState(
            initialDeck,
            cardsOnTable,
            firstPlayer,
            playersState = players
                .zip(cardsPerPlayer)
                .associate { pair -> Pair(pair.first, PlayerState(pair.second)) }
        )

        next(initialState, players)
    }

    private tailrec fun next(state: GameState, allPlayers: List<Player>) {
        io.write(Messages.TURN_SEPARATOR)
        io.write(Messages.cardsOnTable(state.cardsOnTable))

        if (state.handsAreEmpty() && state.deck.isEmpty()) {
            terminate(state)
            return
        }

        val (currentPlayersState, currentDeck) = dealCardsIfEmpty(state)
        val playerState = currentPlayersState.getValue(state.currentPlayer)
        val pickedCard = state.currentPlayer.chooseCard(state.cardsOnTable, playerState.cardsInHand)

        if (pickedCard == null) {
            io.write(Messages.GAME_OVER)
            return
        }

        val playerWonCards = state.cardsOnTable.last().run { rank == pickedCard.rank || suit == pickedCard.suit }

        if (playerWonCards) {
            io.write(Messages.playerWins(state.currentPlayer))
            // todo
        }

        val nextState = GameState(
            deck = currentDeck,
            cardsOnTable = state.cardsOnTable + pickedCard,
            playersState = currentHands + (state.currentPlayer to playerState.minus(pickedCard)),
            currentPlayer = selectNextPlayer(state.currentPlayer, allPlayers)
        )

        next(nextState, allPlayers)
    }

    private fun terminate(finalState: GameState) {
        io.write(Messages.GAME_OVER)
        TODO()
    }

    private fun dealCardsIfEmpty(state: GameState): Pair<Map<Player, PlayerState>, Deck> {
        if (!state.handsAreEmpty()) {
            return Pair(state.playersState, state.deck)
        }

        val (dealtCards, newDeck) = dealCards(state.deck, state.playersState.size)

        val playersState = state
            .playersState
            .asSequence()
            .zip(dealtCards.asSequence())
            .map { Pair(it.first.key, it.first.value.copy(cardsInHand = it.second)) }
            .toMap()

        return Pair(playersState, newDeck)
    }

    private fun dealCards(deck: Deck, playersCount: Int): Pair<List<List<Card>>, Deck> {
        val cardsWithDecks = generateSequence(
            seed = Pair(emptyList<Card>(), deck)
        ) { it.second.getCards(numberOfCards = CARDS_PER_HAND) }
            .drop(1)
            .take(playersCount)
            .toList()

        return Pair(
            cardsWithDecks.map { it.first },
            cardsWithDecks.last().second
        )
    }

    private fun selectNextPlayer(current: Player, allPlayers: List<Player>): Player {
        val currentIndex = allPlayers.indexOf(current)
        require(currentIndex != -1) { "List 'allPlayers' is expected to contain 'current'!" }
        val nextIndex = (currentIndex + 1) % allPlayers.size
        return allPlayers[nextIndex]
    }

    companion object {
        const val INIT_CARDS_COUNT = 4
        const val CARDS_PER_HAND = 6
    }
}
