package calculator

object DisplayText {
    fun help() =
        """
        |Welcome to Hyperskill Calculator!
        |Usage:
        |${displayCommands()}
        """.trimMargin()

    private fun displayCommands() =
        Command
            .nonEmptyClasses()
            .map {
                val name = it.commandNameOrNull()
                val displayText = it.commandDisplayText()
                if (name == null) displayText else "/$name - $displayText"
            }
            .sorted()
            .joinToString(separator = System.lineSeparator()) { "    $it" }

    fun exit() = "Bye"
}

object Errors {
    const val UNKNOWN_COMMAND = "Unknown command"
    const val UNKNOWN_IDENTIFIER = "Unknown identifier"

    const val INVALID_INPUT = "Invalid input"
    const val INVALID_IDENTIFIER = "Invalid identifier"
    const val INVALID_COMMAND_INVOCATION = "Invalid command invocation"
    const val INVALID_ASSIGNMENT = "Invalid assignment"

    const val UNEXPECTED_EOF = "Unexpected end of input!"

    fun unexpectedChar(char: Char) = "Unexpected character '$char'"

    fun unexpectedToken(token: Token?) = "Unexpected token '$token'"
}
