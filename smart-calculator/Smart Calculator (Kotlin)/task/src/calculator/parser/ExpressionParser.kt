package calculator.parser

import arrow.core.Either
import arrow.core.raise.either
import calculator.Command

object ExpressionParser : CommandParser<Command.EvalExpression> {

    override fun canTry(tokens: List<Token>) = tokens.isNotEmpty() && tokens[0] != Token.Slash

    override fun parse(tokens: List<Token>): Either<ParseError, Command.EvalExpression> = either {
        val infixTokens = ReversePolishNotationConverter.convertFromInfixToPostfix(tokens).bind()
        TODO()
    }
}
