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

    override fun parse(tokens: List<Token>): Either<ParserError, Command.EvalExpression> = either {
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

    fun parse(tokens: List<Token>): Either<ParserError, Expression> = either {
        ensure(tokens.isNotEmpty()) { Errors.emptyInput() }
        val infixTokens = convertFromInfixToPostfix(tokens).bind()
        return Expression(infixTokens).right()
    }

    private fun convertFromInfixToPostfix(tokens: List<Token>): Either<ParserError, List<ExpressionTerm>> = either {
        val operatorsStack = ArrayDeque<OpToken>()
        val expressionTerms = mutableListOf<ExpressionTerm>()
        fun unexpected(): Nothing = raise(Errors.unexpectedToken())

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
                        val opToken = operatorsStack.removeLastOrNull() ?: unexpected()
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
                    val isUnary = isUnaryAt(tokens, index)
                    ensure(token in if (isUnary) tokensToUnaryOps else tokensToBinaryOps) { Errors.unexpectedToken() }
                    val opToken = OpToken(token, !isUnary)

                    if (operatorsStack.isEmpty()
                        || operatorsStack.last().token == Token.OpeningParen
                    ) {
                        operatorsStack.addLast(opToken)
                        continue
                    }

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

                Token.Equals -> unexpected()
            }
        }

        repeat(operatorsStack.size) {
            val opToken = operatorsStack.removeLast()
            ensure(opToken.token != Token.OpeningParen) { Errors.unbalancedParens() }
            val operator = opToken.getOperator().bind()
            expressionTerms.add(ExpressionTerm.Op(operator))
        }

        return expressionTerms.right()
    }

    private fun isUnaryAt(tokens: List<Token>, index: Int): Boolean {
        if (index !in 0..<tokens.lastIndex || tokens[index] !in tokensToUnaryOps) {
            return false
        }

        fun Token.isOperator() = this in tokensToUnaryOps || this in tokensToBinaryOps

        val previousTokenIsValid =
            index == 0 || tokens[index - 1].isOperator() || tokens[index - 1] == Token.OpeningParen

        val nextTokenIsValid = tokens[index + 1].let {
            it is Token.Number || it is Token.Word || it is Token.OpeningParen
        } || isUnaryAt(tokens, index + 1)

        return previousTokenIsValid && nextTokenIsValid
    }

    private fun OpToken.getOperator(): Either<ParserError, Operator> {
        val targetMap = if (isBinary) tokensToBinaryOps else tokensToUnaryOps
        return targetMap[token]?.right() ?: Errors.unexpectedToken().left()
    }

    private fun OpToken.getPrecedence() = either {
        val operator = getOperator().bind()
        operator.precedence
    }

    private fun hasToMoveTopOperator(topOperator: OpToken, currentOperator: Operator): Either<ParserError, Boolean> =
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
