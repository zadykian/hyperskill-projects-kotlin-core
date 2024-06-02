package calculator.parser

import arrow.core.raise.Raise
import calculator.Command

interface CommandParser<out TCommand : Command> {
    fun canTry(tokens: List<Token>): Boolean
    context(Raise<ParserError>) fun parse(tokens: List<Token>): TCommand
}

object Parser {
    private val parsers = listOf(
        NamedCommandParser,
        AssignmentCommandParser,
        ExpressionCommandParser,
    )

    context(Raise<Error>)
    fun parse(tokens: List<Token>): Command =
        parsers
            .firstOrNull { it.canTry(tokens) }
            ?.parse(tokens)
            ?: raise(Errors.invalidInput())
}