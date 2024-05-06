package calculator

enum class Operator {
    Plus,
    Minus,
}

typealias Value = Int

sealed interface Expression {
    class Number(val value: Value) : Expression
    class Variable(val identifier: Identifier) : Expression
    class Unary(val opertr: Operator, val operand: Expression) : Expression
    class Binary(val opertr: Operator, val left: Expression, val right: Expression) : Expression
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
            if (regex.matches(string)) Success(Identifier(string))
            else Failure("Identifier can contain only Latin characters!")
    }
}