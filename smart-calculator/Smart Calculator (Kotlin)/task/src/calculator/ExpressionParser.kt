package calculator

object ExpressionParser {
    // E -> T { +|- T }*
    // T -> { +|- }* { 0..9 }+

    fun parse(tokens: TokenSequence): Result<Expression> {
        val iterator = tokens.iterator()
        var root = parseUnary(iterator)

        while (root != null && iterator.hasNext()) {
            root = when (iterator.next()) {
                is Token.Plus -> parseUnary(iterator).onSuccess {
                    Expression.Binary(Operator.Plus, root, it)
                }

                is Token.Minus -> parseUnary(iterator).onSuccess {
                    Expression.Binary(Operator.Minus, root, it)
                }

                else -> null
            }
        }

        return root
    }

    private fun parseUnary(iterator: TokenIterator): Result<Expression> =
        when (val nextToken = iterator.nextOrNull()) {
            is Token.Plus -> parseUnary(iterator).map {
                Expression.Unary(Operator.Plus, it)
            }

            is Token.Minus -> parseUnary(iterator).map {
                Expression.Unary(Operator.Minus, it)
            }

            is Token.Number -> Expression.Number(nextToken.value.toInt()).success()

            else -> Failure(DisplayText.Errors.unexpectedToken(nextToken))
        }
}
