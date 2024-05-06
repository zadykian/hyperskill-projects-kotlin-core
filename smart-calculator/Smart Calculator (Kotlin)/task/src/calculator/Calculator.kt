package calculator

class Calculator {
    private val declaredVariables = mutableMapOf<Identifier, Value>()

    fun assign(identifier: Identifier, expression: Expression): Result<Any> =
        evaluate(expression).onSuccess { declaredVariables[identifier] = it }

    fun evaluate(expression: Expression): Result<Value> =
        when (expression) {
            is Expression.Number -> Success(expression.value)

            is Expression.Variable ->
                declaredVariables[expression.identifier]?.let { Success(it) }
                    ?: Failure(Errors.UNKNOWN_IDENTIFIER)

            is Expression.Unary -> when (expression.opertr) {
                Operator.Plus -> evaluate(expression.operand)
                Operator.Minus -> evaluate(expression.operand).map { it.unaryMinus() }
            }

            is Expression.Binary -> {
                val operation: (Pair<Value, Value>) -> Value = when (expression.opertr) {
                    Operator.Plus -> { p -> p.first + p.second }
                    Operator.Minus -> { p -> p.first - p.second }
                }

                evaluate(expression.left)
                    .bind { leftVal ->
                        evaluate(expression.right).map { rightVal -> Pair(leftVal, rightVal) }
                    }
                    .map(operation)
            }
        }
}
