package indigo

import indigo.GameProceeded.CardPlayed
import indigo.GameProceeded.InitialCardsPlaced

interface GameEventHandler {
    fun handle(event: GameEvent)
}

class IoGameEventHandler(private val io: IO) : GameEventHandler {
    override fun handle(event: GameEvent) =
        when (event) {
            is GameCreated -> io.write(Messages.GREETING)

            is InitialCardsPlaced -> {
                io.write(Messages.initialCards(event.nextState.cardsOnTable))
                writeCardsOnTable(event.nextState)
            }

            is CardPlayed -> {
                writePlayedCards(event)
                if (event.isWon) {
                    io.write(Messages.playerWins(event.playedBy))
                    writeCurrentScore(event.nextState)
                }
                writeCardsOnTable(event.nextState)
            }

            is GameCompleted -> {
                writeCurrentScore(event.finalState)
                writeGameOver()
            }

            is GameTerminated -> writeGameOver()

            else -> {}
        }

    private fun writePlayedCards(event: CardPlayed) {
        if (!event.playedBy.displayPlayedCards) {
            return
        }

        val usedCards = event.previousState.playersState.getValue(event.playedBy).cardsInHand
        io.write(Messages.cards(usedCards))
        io.write(Messages.cardPlayed(event.playedBy, event.pickedCard))
    }

    private fun writeCardsOnTable(nextState: GameState) {
        io.write(Messages.LINE_SEPARATOR)
        io.write(Messages.cardsOnTable(nextState.cardsOnTable))
    }

    private fun writeCurrentScore(nextState: GameState) = io.write(Messages.currentScore(nextState))

    private fun writeGameOver() = io.write(Messages.GAME_OVER)
}
