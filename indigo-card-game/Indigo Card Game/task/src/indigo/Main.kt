package indigo

fun main() {
    val io = object : IO {
        override fun read() = readln()
        override fun write(value: String) = println(value)
    }

    tailrec fun selectFirstPlayer(players: List<Player>): Player? {
        io.write(Messages.PLAY_FIRST_REQUEST)
        return when (io.read().lowercase()) {
            Answers.YES -> players.single { it is User }
            Answers.NO -> players.single { it is Computer }
            Answers.EXIT -> null
            else -> selectFirstPlayer(players)
        }
    }

    val gameStateHandler = IoGameEventHandler(io)

    Game(gameStateHandler).run(
        players = listOf(User(io, name = "Player"), Computer()),
        firstPlayerSelector = ::selectFirstPlayer,
    )
}
