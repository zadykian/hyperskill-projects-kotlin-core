package calculator.parser

object Errors {
    const val UNKNOWN_COMMAND = "Unknown command"
    const val UNKNOWN_IDENTIFIER = "Unknown identifier"

    const val INVALID_INPUT = "Invalid input"
    const val INVALID_IDENTIFIER = "Invalid identifier"
    const val INVALID_COMMAND_INVOCATION = "Invalid command invocation"
    const val INVALID_ASSIGNMENT = "Invalid assignment"
    const val INVALID_EXPRESSION = "Invalid expression"
    const val UNBALANCED_PARENS_IN_EXPRESSION = "Unbalanced parens in the expression"

    fun unexpectedChar(char: Char) = "Unexpected character '$char'"

    fun unexpectedToken(token: Token?) = "Unexpected token '$token'"
}
