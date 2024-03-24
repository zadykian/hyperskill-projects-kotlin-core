package indigo

class IO(val read: () -> String, val write: (String) -> Unit)

fun main() {
    val io = IO(read = { readln() }, write = { println(it) })
    val actionReceiver = IOActionReceiver(io)
    Game(actionReceiver, io).run(listOf(User(io), Computer()))
}
