package calculator

import kotlin.math.pow

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

            is Expression.Unary -> when (expression.operator) {
                Operator.Unary.Plus -> evaluate(expression.operand)
                Operator.Unary.Negation -> evaluate(expression.operand).map { it.unaryMinus() }
            }

            is Expression.Binary ->
                evaluate(expression.left)
                    .bind { leftVal -> evaluate(expression.right).map { rightVal -> Pair(leftVal, rightVal) } }
                    .map(operationOf(expression.operator))
        }

    private fun operationOf(operator: Operator.Binary): (Pair<Value, Value>) -> Value =
        when (operator) {
            Operator.Binary.Addition -> { p -> p.first + p.second }
            Operator.Binary.Subtraction -> { p -> p.first - p.second }
            Operator.Binary.Multiplication -> { p -> p.first * p.second }
            Operator.Binary.Division -> { p -> p.first / p.second }
            Operator.Binary.Power -> { p -> p.first.toDouble().pow(p.second.toDouble()).toInt() }
        }
}
