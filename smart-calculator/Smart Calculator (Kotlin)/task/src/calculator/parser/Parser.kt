package calculator.parser

import arrow.core.Either
import arrow.core.left
import calculator.Command

typealias ParseError = String

interface CommandParser<out TCommand : Command> {
    fun canTry(tokens: List<Token>): Boolean
    fun parse(tokens: List<Token>): Either<ParseError, TCommand>
}

object Parser {
    private val parsers = listOf(
        NamedCommandParser,
        AssignmentParser,
        ExpressionParser,
    )

    fun parse(tokens: List<Token>): Either<ParseError, Command> =
        parsers
            .firstOrNull { it.canTry(tokens) }
            ?.parse(tokens)
            ?: Errors.INVALID_INPUT.left()
}