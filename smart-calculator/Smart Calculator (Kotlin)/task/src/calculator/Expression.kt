package calculator

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import calculator.parser.Errors

enum class Associativity {
    Left,
    Right
}

sealed interface Operator {
    val precedence: Byte
    val associativity: Associativity

    enum class Unary(override val precedence: Byte, override val associativity: Associativity) : Operator {
        Plus(2, Associativity.Right),
        Negate(2, Associativity.Right),
    }

    enum class Binary(override val precedence: Byte, override val associativity: Associativity) : Operator {
        Add(0, Associativity.Left),
        Subtract(0, Associativity.Left),
        Multiply(1, Associativity.Left),
        Divide(1, Associativity.Left),
        Power(2, Associativity.Right),
    }
}

typealias Value = Int

sealed interface Expression {
    class Number(val value: Value) : Expression
    class Variable(val identifier: Identifier) : Expression
    class Unary(val operator: Operator.Unary, val operand: Expression) : Expression
    class Binary(val operator: Operator.Binary, val left: Expression, val right: Expression) : Expression
}

class Identifier private constructor(private val value: String) {
    override fun toString() = value

    override fun hashCode() = value.hashCode()

    override fun equals(other: Any?) = when {
        this === other -> true
        other is Identifier -> value == other.value
        else -> false
    }

    companion object {
        private val regex = Regex("^[a-zA-Z]+$")

        fun tryParse(string: String): Either<String, Identifier> =
            if (regex.matches(string)) Identifier(string).right()
            else Errors.INVALID_IDENTIFIER.left()
    }
}
