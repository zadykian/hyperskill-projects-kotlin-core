package calculator

import arrow.core.raise.Raise
import calculator.ExpressionTerm.*
import calculator.parser.Error
import calculator.parser.Errors
import kotlin.math.pow

class Calculator {
    private val declaredVariables = mutableMapOf<Identifier, Value>()

    context(Raise<Error>)
    fun assign(identifier: Identifier, expression: Expression) {
        val value = evaluate(expression)
        declaredVariables[identifier] = value
    }

    context(Raise<Error>)
    fun evaluate(expression: Expression): Value {
        val evalStack = ArrayDeque<Value>()

        for (term in expression.postfixTerms) {
            when (term) {
                is Num -> evalStack.addLast(term.value)
                is Id -> {
                    val variableValue = declaredVariables[term.value] ?: raise(Errors.unknownIdentifier())
                    evalStack.addLast(variableValue)
                }

                is Op -> handleOperator(term, evalStack)
            }
        }

        return if (evalStack.size == 1) evalStack.first() else raise(Errors.invalidExpression())
    }

    context(Raise<Error>)
    private fun Calculator.handleOperator(operator: Op, evalStack: ArrayDeque<Value>) =
        when (operator.value) {
            is Operator.Unary -> {
                val operand = evalStack.popValue()
                val value = operationOf(operator.value)(operand)
                evalStack.addLast(value)
            }

            is Operator.Binary -> {
                val rightOperand = evalStack.popValue()
                val leftOperand = evalStack.popValue()
                val value = operationOf(operator.value)(leftOperand, rightOperand)
                evalStack.addLast(value)
            }
        }

    context(Raise<Error>)
    private fun ArrayDeque<Value>.popValue() = removeLastOrNull() ?: raise(Errors.invalidExpression())

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
