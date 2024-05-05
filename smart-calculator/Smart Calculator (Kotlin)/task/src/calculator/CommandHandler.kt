package calculator

class CommandHandler(private val io: IO) {
    fun handle(commandType: CommandType) =
        when (commandType) {
            CommandType.Help -> io.write(DisplayText.helpText)
            CommandType.Exit -> io.write(DisplayText.EXIT_TEXT)
        }

    private object DisplayText {
        val helpText =
            """
            Welcome to Hyperskill Calculator!
            Usage:
                [expression] - evaluate an arithmetic expression. Example: 2 + 4 - 1
                /help - display help info
                /exit - exit program
            """.trimIndent()

        const val EXIT_TEXT = "Bye"
    }
}
