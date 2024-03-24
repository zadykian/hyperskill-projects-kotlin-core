package indigo

interface ActionReceiver {
    fun next(): UserAction
}

class IOActionReceiver(private val io: IO) : ActionReceiver {
    override tailrec fun next(): UserAction = when (nextInputLowercase()) {
        "reset" -> UserAction.Reset

        "shuffle" -> UserAction.Shuffle

        "get" -> {
            io.write(Messages.NUM_OF_CARDS_REQUEST)
            val numberOfCards = io.read().toIntOrNull()
            if (numberOfCards == null) {
                io.write(Messages.INVALID_NUMBER_OF_CARDS)
                next()
            } else {
                UserAction.GetCards(numberOfCards)
            }
        }

        "exit" -> UserAction.Exit

        else -> {
            io.write(Messages.WRONG_ACTION)
            next()
        }
    }

    private fun nextInputLowercase(): String {
        io.write(Messages.CHOOSE_ACTION_REQUEST)
        return io.read().lowercase()
    }
}