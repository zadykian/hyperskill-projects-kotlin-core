package calculator

class Calculator {
    private val declaredVariables = mutableMapOf<Identifier, Value>()

    fun assign(assignment: Assignment): Result<Any> =
        evaluate(assignment.expression).onSuccess { declaredVariables[assignment.identifier] = it }

    fun evaluate(expression: Expression): Result<Value> =
        when (expression) {
            is Expression.Number -> Success(expression.value)

            is Expression.Variable ->
                declaredVariables[expression.identifier]?.let { Success(it) }
                    ?: Failure(DisplayText.Errors.UNKNOWN_IDENTIFIER)

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
