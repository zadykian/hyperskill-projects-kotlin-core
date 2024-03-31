package indigo

import indigo.GameProceeded.*

interface GameStateHandler {
    fun onStateChanged(nextState: GameState)
}

class IoGameStateHandler(private val io: IO) : GameStateHandler {
    override fun onStateChanged(nextState: GameState) =
        when (val event = nextState.parentEvent) {
            is InitialCardsPlaced -> {
                io.write(Messages.initialCards(nextState.cardsOnTable))
                writeCardsOnTable(nextState)
            }

            is CardWon -> {
                writePlayedCards(event)
                io.write(Messages.playerWins(event.playedBy))
                writeCurrentScore(nextState)
                writeCardsOnTable(nextState)
            }

            is CardLost -> {
                writePlayedCards(event)
                writeCardsOnTable(nextState)
            }

            is GameCompleted -> writeCurrentScore(nextState)

            else -> {}
        }

    private fun writePlayedCards(event: CardPlayed) {
        if (!event.playedBy.displayPlayedCards) {
            return
        }

        val cardsInHand = event.previous.playersState.getValue(event.playedBy).cardsInHand
        io.write(Messages.cards(cardsInHand))
        io.write(Messages.cardPlayed(event.playedBy, event.pickedCard))
    }

    private fun writeCardsOnTable(nextState: GameState) {
        io.write(Messages.LINE_SEPARATOR)
        io.write(Messages.cardsOnTable(nextState.cardsOnTable))
    }

    private fun writeCurrentScore(nextState: GameState) = io.write(Messages.currentScore(nextState))
}
