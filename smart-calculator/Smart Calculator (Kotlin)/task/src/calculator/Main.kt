package calculator

fun main() {
    val io = object : IO {
        override fun read() = readln()
        override fun write(value: String) = println(value)
    }

    val application = Application(
        CommandHandler(io),
        io
    )

    application.run()
}