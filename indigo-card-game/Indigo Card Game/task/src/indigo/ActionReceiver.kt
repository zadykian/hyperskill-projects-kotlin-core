package indigo

interface ActionReceiver {
    fun next(): UserAction
}

class IOActionReceiver(
    private val inputReader: InputReader,
    private val outputWriter: OutputWriter
) : ActionReceiver {
    override tailrec fun next(): UserAction = when (nextInputLowercase()) {
        "reset" -> UserAction.Reset

        "shuffle" -> UserAction.Shuffle

        "get" -> {
            outputWriter(Messages.NUM_OF_CARDS_REQUEST)
            val numberOfCards = inputReader().toIntOrNull()
            if (numberOfCards == null) {
                outputWriter(Messages.INVALID_NUMBER_OF_CARDS)
                next()
            } else {
                UserAction.GetCards(numberOfCards)
            }
        }

        "exit" -> UserAction.Exit

        else -> {
            outputWriter(Messages.WRONG_ACTION)
            next()
        }
    }

    private fun nextInputLowercase(): String {
        outputWriter(Messages.CHOOSE_ACTION_REQUEST)
        return inputReader().lowercase()
    }
}