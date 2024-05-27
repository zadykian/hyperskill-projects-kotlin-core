package calculator

enum class UnaryOp {
    Plus,
    Negation,
}

enum class BinaryOp {
    Addition,
    Subtraction,
    Multiplication,
    Division,
    Power,
}

typealias Value = Int

sealed interface Expression {
    class Number(val value: Value) : Expression
    class Variable(val identifier: Identifier) : Expression
    class Unary(val operator: UnaryOp, val operand: Expression) : Expression
    class Binary(val operator: BinaryOp, val left: Expression, val right: Expression) : Expression
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