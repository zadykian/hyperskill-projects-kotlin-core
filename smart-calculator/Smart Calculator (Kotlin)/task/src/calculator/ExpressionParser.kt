package calculator

object ExpressionParser {
    // E -> T { +|- T }*
    // T -> { +|- }* { 0..9 }+

    fun parse(tokens: Sequence<Token>): Expression? {
        val iterator = tokens.iterator()
        var root = parseUnary(iterator)

        while (root != null && iterator.hasNext()) {
            root = when (iterator.next()) {
                is Token.Plus -> parseUnary(iterator)?.let {
                    Expression.Binary(Operator.Plus, root!!, it)
                }

                is Token.Minus -> parseUnary(iterator)?.let {
                    Expression.Binary(Operator.Minus, root!!, it)
                }

                else -> null
            }
        }

        return root
    }

    private fun parseUnary(iterator: Iterator<Token>): Expression? =
        when (val nextToken = iterator.nextOrNull()) {
            is Token.Plus -> parseUnary(iterator)?.let {
                Expression.Unary(Operator.Plus, it)
            }

            is Token.Minus -> parseUnary(iterator)?.let {
                Expression.Unary(Operator.Minus, it)
            }

            is Token.Number -> Expression.Number(nextToken.value)
            null -> null
        }

    private fun Iterator<Token>.nextOrNull() = if (hasNext()) next() else null
}
