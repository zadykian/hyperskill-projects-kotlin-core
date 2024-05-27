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
                UnaryOp.Plus -> evaluate(expression.operand)
                UnaryOp.Negation -> evaluate(expression.operand).map { it.unaryMinus() }
            }

            is Expression.Binary ->
                evaluate(expression.left)
                    .bind { leftVal -> evaluate(expression.right).map { rightVal -> Pair(leftVal, rightVal) } }
                    .map(operationOf(expression.operator))
        }

    private fun operationOf(operator: BinaryOp): (Pair<Value, Value>) -> Value =
        when (operator) {
            BinaryOp.Addition -> { p -> p.first + p.second }
            BinaryOp.Subtraction -> { p -> p.first - p.second }
            BinaryOp.Multiplication -> { p -> p.first * p.second }
            BinaryOp.Division -> { p -> p.first / p.second }
            BinaryOp.Power -> { p -> p.first.toDouble().pow(p.second.toDouble()).toInt() }
        }
}
