package indigo

typealias InputReader = () -> String
typealias OutputWriter = (String) -> Unit

fun main() {
    val inputReader: InputReader = { readln() }
    val outputWriter: OutputWriter = { println(it) }
    val actionReceiver = IOActionReceiver(inputReader, outputWriter)
    Game(actionReceiver, outputWriter).run()
}