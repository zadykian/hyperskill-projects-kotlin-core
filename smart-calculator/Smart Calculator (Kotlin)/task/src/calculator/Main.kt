package calculator

fun main() {
    val application = Application(
        Calculator(),
        IO(read = ::readln, write = ::println)
    )

    application.run()
}
