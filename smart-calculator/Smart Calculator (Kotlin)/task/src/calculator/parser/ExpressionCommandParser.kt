package calculator.parser

import arrow.core.raise.Raise
import arrow.core.raise.ensure
import calculator.*

private data class OpToken(val token: Token, val isBinary: Boolean)

object ExpressionCommandParser : CommandParser<Command.EvalExpression> {
    override fun canTry(tokens: List<Token>) = tokens.isNotEmpty() && tokens[0] != Token.Slash

    context(Raise<ParserError>)
    override fun parse(tokens: List<Token>): Command.EvalExpression {
        val expression = ExpressionParser.parse(tokens)
        return Command.EvalExpression(expression)
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

    context(Raise<ParserError>)
    fun parse(tokens: List<Token>): Expression {
        ensure(tokens.isNotEmpty()) { Errors.emptyInput() }
        val infixTokens = convertFromInfixToPostfix(tokens)
        return Expression(infixTokens)
    }

    context(Raise<ParserError>)
    private fun convertFromInfixToPostfix(tokens: List<Token>): List<ExpressionTerm> {
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

                        val operator = opToken.getOperator()
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

                    val currentOperator = opToken.getOperator()

                    while (
                        operatorsStack.isNotEmpty()
                        && hasToMoveTopOperator(operatorsStack.last(), currentOperator)
                    ) {
                        val removedOperator = operatorsStack.removeLast().getOperator()
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
            val operator = opToken.getOperator()
            expressionTerms.add(ExpressionTerm.Op(operator))
        }

        return expressionTerms
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

    context(Raise<ParserError>)
    private fun OpToken.getOperator(): Operator {
        val targetMap = if (isBinary) tokensToBinaryOps else tokensToUnaryOps
        return targetMap[token] ?: raise(Errors.unexpectedToken())
    }

    context(Raise<ParserError>)
    private fun hasToMoveTopOperator(topOperator: OpToken, currentOperator: Operator): Boolean {
        if (topOperator.token == Token.OpeningParen) {
            return false
        }

        val topPrecedence = topOperator.getOperator().precedence

        val hasGreaterOrEqualPriority = topPrecedence > currentOperator.precedence
                || topPrecedence == currentOperator.precedence && currentOperator.associativity == Associativity.Left

        return hasGreaterOrEqualPriority
    }
}
