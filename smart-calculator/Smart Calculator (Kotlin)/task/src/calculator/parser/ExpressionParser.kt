package calculator.parser

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.right
import calculator.Command
import calculator.Identifier
import calculator.Operator
import calculator.Value

private data class OpToken(val token: Token, val isBinary: Boolean)

private interface PostfixTerm {
    @JvmInline
    value class Number(val value: Value) : PostfixTerm

    @JvmInline
    value class Identifier(val value: calculator.Identifier) : PostfixTerm

    @JvmInline
    value class Operator(val value: calculator.Operator) : PostfixTerm
}

object ExpressionParser : CommandParser<Command.EvalExpression> {
    private val tokensToUnaryOps = mapOf(
        Token.Plus to Operator.Unary.Plus,
        Token.Minus to Operator.Unary.Negation,
    )

    private val tokensToBinaryOps = mapOf(
        Token.Plus to Operator.Binary.Addition,
        Token.Minus to Operator.Binary.Subtraction,
        Token.Asterisk to Operator.Binary.Multiplication,
        Token.Slash to Operator.Binary.Division,
        Token.Attic to Operator.Binary.Power,
    )

    private fun Token.isOperator() = this in tokensToUnaryOps || this in tokensToBinaryOps

    private fun OpToken.getOperator(): Either<ParseError, Operator> {
        val targetMap = if (isBinary) tokensToBinaryOps else tokensToUnaryOps
        return targetMap[token]?.right() ?: Errors.unexpectedToken(token).left()
    }

    private fun OpToken.getPrecedence() = either {
        val operator = getOperator().bind()
        operator.precedence
    }

    override fun canTry(tokens: List<Token>) = tokens.isNotEmpty() && tokens[0] != Token.Slash

    override fun parse(tokens: List<Token>): Either<ParseError, Command.EvalExpression> = either {
        val infixTokens = convertFromInfixToPostfix(tokens).bind()

        TODO()
    }

    private fun convertFromInfixToPostfix(tokens: List<Token>): Either<ParseError, List<PostfixTerm>> = either {
        val operatorsStack = ArrayDeque<OpToken>()
        val postfixTerms = mutableListOf<PostfixTerm>()
        fun unexpected(token: Token): Nothing = raise(Errors.unexpectedToken(token))

        for (index in 0..tokens.lastIndex) {
            when (val token = tokens[index]) {
                is Token.Number -> postfixTerms.add(PostfixTerm.Number(token.value))
                is Token.Word -> {
                    val identifier = Identifier.tryParse(token.value).bind()
                    postfixTerms.add(PostfixTerm.Identifier(identifier))
                }

                Token.OpeningParen -> operatorsStack.addLast(OpToken(token, false))
                Token.ClosingParen -> {
                    while (true) {
                        val opToken = operatorsStack.removeLastOrNull() ?: unexpected(token)
                        if (opToken.token is Token.OpeningParen) {
                            break
                        }

                        val operator = opToken.getOperator().bind()
                        postfixTerms.add(PostfixTerm.Operator(operator))
                    }
                }

                Token.Plus,
                Token.Slash,
                Token.Minus,
                Token.Asterisk,
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

                    val currentPrecedence = opToken.getPrecedence().bind()

                    while (
                        operatorsStack.isNotEmpty()
                        && operatorsStack.last().let {
                            it.token != Token.OpeningParen && it.getPrecedence().bind() >= currentPrecedence
                        }
                    ) {
                        val removedOperator = operatorsStack.removeLast().getOperator().bind()
                        postfixTerms.add(PostfixTerm.Operator(removedOperator))
                    }

                    operatorsStack.addLast(opToken)
                }

                Token.Equals -> unexpected(token)
            }
        }

        return postfixTerms.right()
    }
}
