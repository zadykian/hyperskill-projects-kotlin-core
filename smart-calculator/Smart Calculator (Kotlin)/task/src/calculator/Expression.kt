package calculator

sealed interface Operator {
    val priority: Byte

    enum class Unary(override val priority: Byte) : Operator {
        Plus(2),
        Negation(2),
    }

    enum class Binary(override val priority: Byte) : Operator {
        Addition(0),
        Subtraction(0),
        Multiplication(1),
        Division(1),
        Power(2),
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

        fun tryParse(string: String): Result<Identifier> =
            if (regex.matches(string)) Identifier(string).success()
            else Errors.INVALID_IDENTIFIER.failure()
    }
}