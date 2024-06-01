package calculator.parser

import arrow.core.Either
import arrow.core.left
import calculator.Command

interface CommandParser<out TCommand : Command> {
    fun canTry(tokens: List<Token>): Boolean
    fun parse(tokens: List<Token>): Either<ParserError, TCommand>
}

object Parser {
    private val parsers = listOf(
        NamedCommandParser,
        AssignmentCommandParser,
        ExpressionCommandParser,
    )

    fun parse(tokens: List<Token>): Either<Error, Command> =
        parsers
            .firstOrNull { it.canTry(tokens) }
            ?.parse(tokens)
            ?: Errors.invalidInput().left()
}