package calculator.parser

sealed interface Error {
    val displayText: String
}

data class LexerError(override val displayText: String) : Error
data class ParserError(override val displayText: String) : Error
data class CalculatorError(override val displayText: String) : Error

object Errors {
    fun unknownIdentifier() = CalculatorError("Unknown identifier")

    fun emptyInput() = ParserError("Empty input")
    fun invalidInput() = ParserError("Invalid input")
    fun unknownCommand() = ParserError("Unknown command")
    fun invalidCommandInvocation() = ParserError("Invalid command invocation")

    fun invalidIdentifier() = ParserError("Invalid identifier")
    fun invalidAssignment() = ParserError("Invalid assignment")
    fun invalidExpression() = ParserError("Invalid expression")
    fun unbalancedParens() = ParserError("Invalid expression")

    fun unexpectedChar(char: Char) = LexerError("Unexpected character '$char'")

    fun unexpectedToken() = ParserError("Invalid expression")
}
