package calculator

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import calculator.parser.Errors
import kotlin.math.pow

typealias CalculationError = String

class Calculator {
    private val declaredVariables = mutableMapOf<Identifier, Value>()

    fun assign(identifier: Identifier, expression: Expression) =
        evaluate(expression).onRight { declaredVariables[identifier] = it }

    fun evaluate(expression: Expression): Either<CalculationError, Value> =
        when (expression) {
            is Expression.Number -> expression.value.right()

            is Expression.Variable ->
                declaredVariables[expression.identifier]?.right() ?: Errors.UNKNOWN_IDENTIFIER.left()

            is Expression.Unary -> when (expression.operator) {
                Operator.Unary.Plus -> evaluate(expression.operand)
                Operator.Unary.Negation -> evaluate(expression.operand).map { it.unaryMinus() }
            }

            is Expression.Binary -> either {
                val left = evaluate(expression.left).bind()
                val right = evaluate(expression.right).bind()
                operationOf(expression.operator).invoke(left, right)
            }
        }

    private fun operationOf(operator: Operator.Binary): (Value, Value) -> Value =
        when (operator) {
            Operator.Binary.Addition -> { l, r -> l + r }
            Operator.Binary.Subtraction -> { l, r -> l - r }
            Operator.Binary.Multiplication -> { l, r -> l * r }
            Operator.Binary.Division -> { l, r -> l / r }
            Operator.Binary.Power -> { l, r -> l.toDouble().pow(r.toDouble()).toInt() }
        }
}
