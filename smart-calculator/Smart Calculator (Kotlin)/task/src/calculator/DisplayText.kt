package calculator

object DisplayText {
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

object Errors {
    const val UNKNOWN_COMMAND = "Unknown command"
    const val UNKNOWN_IDENTIFIER = "Unknown identifier"

    const val INVALID_IDENTIFIER = "Invalid identifier"
    const val INVALID_ASSIGNMENT = "Invalid assignment"

    const val UNEXPECTED_EOF = "Unexpected end of input!"

    fun unexpectedChar(char: Char, position: Int) = "Unexpected character '$char' at position '$position'"

    fun unexpectedToken(token: Token?) = "Unexpected token '$token'"
}
