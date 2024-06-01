package calculator.parser

sealed interface Error {
    val displayText: String
}

class LexerError(override val displayText: String) : Error
class ParserError(override val displayText: String) : Error
class CalculatorError(override val displayText: String) : Error

object Errors {
    fun unknownIdentifier() = CalculatorError("Unknown identifier")

    fun invalidInput() = ParserError("Invalid input")
    fun unknownCommand() = ParserError("Unknown command")
    fun invalidCommandInvocation() = ParserError("Invalid command invocation")

    fun invalidIdentifier() = ParserError("Invalid identifier")
    fun invalidAssignment() = ParserError("Invalid assignment")
    fun invalidExpression() = ParserError("Invalid expression")
    fun unbalancedParens() = ParserError("Unbalanced parens in the expression")

    fun unexpectedChar(char: Char) = LexerError("Unexpected character '$char'")

    fun unexpectedToken(token: Token?) = ParserError("Unexpected token '$token'")
}
