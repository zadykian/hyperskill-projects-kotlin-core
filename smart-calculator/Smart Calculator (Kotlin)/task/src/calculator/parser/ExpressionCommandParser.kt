package calculator.parser

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.right
import calculator.*

private data class OpToken(val token: Token, val isBinary: Boolean)

object ExpressionCommandParser : CommandParser<Command.EvalExpression> {
    override fun canTry(tokens: List<Token>) = tokens.isNotEmpty() && tokens[0] != Token.Slash

    override fun parse(tokens: List<Token>): Either<ParseError, Command.EvalExpression> = either {
        val expression = ExpressionParser.parse(tokens).bind()
        return Command.EvalExpression(expression).right()
    }
}

object ExpressionParser {
    private val tokensToUnaryOps = mapOf(
        Token.Plus to Operator.Unary.Plus,
        Token.Minus to Operator.Unary.Negate,
    )

    private val tokensToBinaryOps = mapOf(
        Token.Plus to Operator.Binary.Add,
        Token.Minus to Operator.Binary.Subtract,
        Token.Asterisk to Operator.Binary.Multiply,
        Token.Slash to Operator.Binary.Divide,
        Token.Attic to Operator.Binary.Power,
    )

    fun parse(tokens: List<Token>): Either<ParseError, Expression> = either {
        val infixTokens = convertFromInfixToPostfix(tokens).bind()
        return Expression(infixTokens).right()
    }

    private fun convertFromInfixToPostfix(tokens: List<Token>): Either<ParseError, List<ExpressionTerm>> = either {
        val operatorsStack = ArrayDeque<OpToken>()
        val expressionTerms = mutableListOf<ExpressionTerm>()
        fun unexpected(token: Token): Nothing = raise(Errors.unexpectedToken(token))

        for (index in 0..tokens.lastIndex) {
            when (val token = tokens[index]) {
                is Token.Number -> expressionTerms.add(ExpressionTerm.Num(token.value))
                is Token.Word -> {
                    val identifier = Identifier.tryParse(token.value).bind()
                    expressionTerms.add(ExpressionTerm.Id(identifier))
                }

                Token.OpeningParen -> operatorsStack.addLast(OpToken(token, false))
                Token.ClosingParen -> {
                    while (true) {
                        val opToken = operatorsStack.removeLastOrNull() ?: unexpected(token)
                        if (opToken.token is Token.OpeningParen) {
                            break
                        }

                        val operator = opToken.getOperator().bind()
                        expressionTerms.add(ExpressionTerm.Op(operator))
                    }
                }

                Token.Plus,
                Token.Minus,
                Token.Asterisk,
                Token.Slash,
                Token.Attic -> {
                    val isUnary = token in tokensToUnaryOps
                            && (index == 0 || tokens[index - 1].isOperator() || tokens[index - 1] == Token.OpeningParen)
                    val opToken = OpToken(token, !isUnary)

                    if (operatorsStack.isEmpty()
                        || operatorsStack.last().token == Token.OpeningParen
                    ) {
                        operatorsStack.addLast(opToken)
                        continue
                    }

                    val operatorMap = if (isUnary) tokensToUnaryOps else tokensToBinaryOps
                    ensure(token in operatorMap) { Errors.unexpectedToken(token) }

                    val currentOperator = opToken.getOperator().bind()

                    while (
                        operatorsStack.isNotEmpty()
                        && hasToMoveTopOperator(operatorsStack.last(), currentOperator).bind()
                    ) {
                        val removedOperator = operatorsStack.removeLast().getOperator().bind()
                        expressionTerms.add(ExpressionTerm.Op(removedOperator))
                    }


                    operatorsStack.addLast(opToken)
                }

                Token.Equals -> unexpected(token)
            }
        }

        repeat(operatorsStack.size) {
            val opToken = operatorsStack.removeLast()
            ensure(opToken.token != Token.OpeningParen) { Errors.UNBALANCED_PARENS_IN_EXPRESSION }
            val operator = opToken.getOperator().bind()
            expressionTerms.add(ExpressionTerm.Op(operator))
        }

        return expressionTerms.right()
    }

    private fun Token.isOperator() = this in tokensToUnaryOps || this in tokensToBinaryOps

    private fun OpToken.getOperator(): Either<ParseError, Operator> {
        val targetMap = if (isBinary) tokensToBinaryOps else tokensToUnaryOps
        return targetMap[token]?.right() ?: Errors.unexpectedToken(token).left()
    }

    private fun OpToken.getPrecedence() = either {
        val operator = getOperator().bind()
        operator.precedence
    }

    private fun hasToMoveTopOperator(topOperator: OpToken, currentOperator: Operator): Either<ParseError, Boolean> =
        either {
            if (topOperator.token == Token.OpeningParen) {
                return false.right()
            }

            val topPrecedence = topOperator.getPrecedence().bind()

            val hasGreaterOrEqualPriority = topPrecedence > currentOperator.precedence
                    || topPrecedence == currentOperator.precedence && currentOperator.associativity == Associativity.Left

            return hasGreaterOrEqualPriority.right()
        }
}
