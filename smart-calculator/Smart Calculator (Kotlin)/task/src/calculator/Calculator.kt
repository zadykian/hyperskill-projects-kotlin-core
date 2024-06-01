package calculator

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import calculator.ExpressionTerm.*
import calculator.parser.Error
import calculator.parser.Errors
import kotlin.math.pow

class Calculator {
    private val declaredVariables = mutableMapOf<Identifier, Value>()

    fun assign(identifier: Identifier, expression: Expression) =
        evaluate(expression).onRight { declaredVariables[identifier] = it }

    fun evaluate(expression: Expression): Either<Error, Value> = either {
        val evalStack = ArrayDeque<Value>()

        for (term in expression.postfixTerms) {
            when (term) {
                is Num -> evalStack.addLast(term.value)
                is Id -> {
                    val variableValue = declaredVariables[term.value] ?: return Errors.unknownIdentifier().left()
                    evalStack.addLast(variableValue)
                }

                is Op -> handleOperator(term, evalStack)
            }
        }

        return if (evalStack.size == 1) evalStack.first().right() else Errors.invalidExpression().left()
    }

    private fun Calculator.handleOperator(operator: Op, evalStack: ArrayDeque<Value>) = either {
        when (operator.value) {
            is Operator.Unary -> {
                val operand = evalStack.popValue().bind()
                val value = operationOf(operator.value)(operand)
                evalStack.addLast(value)
            }

            is Operator.Binary -> {
                val rightOperand = evalStack.popValue().bind()
                val leftOperand = evalStack.popValue().bind()
                val value = operationOf(operator.value)(leftOperand, rightOperand)
                evalStack.addLast(value)
            }
        }
    }

    private fun ArrayDeque<Value>.popValue() = removeLastOrNull()?.right() ?: Errors.invalidExpression().left()

    private fun operationOf(operator: Operator.Unary): (Value) -> Value =
        when (operator) {
            Operator.Unary.Plus -> { v -> v }
            Operator.Unary.Negate -> { v -> -v }
        }

    private fun operationOf(operator: Operator.Binary): (Value, Value) -> Value =
        when (operator) {
            Operator.Binary.Add -> { l, r -> l + r }
            Operator.Binary.Subtract -> { l, r -> l - r }
            Operator.Binary.Multiply -> { l, r -> l * r }
            Operator.Binary.Divide -> { l, r -> l / r }
            Operator.Binary.Power -> { l, r -> l.toDouble().pow(r.toDouble()).toInt() }
        }
}
