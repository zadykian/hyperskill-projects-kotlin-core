package calculator

import arrow.core.Either
import arrow.core.raise.either

typealias CalculationError = String

class Calculator {
    private val declaredVariables = mutableMapOf<Identifier, Value>()

    fun assign(identifier: Identifier, expression: Expression) =
        evaluate(expression).onRight { declaredVariables[identifier] = it }

    fun evaluate(expression: Expression): Either<CalculationError, Value> = either {
        TODO()
    }
}
