package indigo

class IO(val read: () -> String, val write: (String) -> Unit)

fun main() {
    val io = IO(read = { readln() }, write = { println(it) })

    fun selectFirstPlayer(players: List<Player>): Player? {
        io.write(Messages.PLAY_FIRST_REQUEST)
        return when (io.read().lowercase()) {
            Answers.YES -> players.single { it is User }
            Answers.NO -> players.single { it is Computer }
            Answers.EXIT -> null
            else -> selectFirstPlayer(players)
        }
    }

    Game(io).run(
        players = listOf(User(io, name = "Player"), Computer(io)),
        firstPlayerSelector = ::selectFirstPlayer
    )
}
