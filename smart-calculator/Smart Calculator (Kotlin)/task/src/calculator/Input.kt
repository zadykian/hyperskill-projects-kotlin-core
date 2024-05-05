package calculator

enum class CommandType {
    Help,
    Exit,
}

enum class ErrorType(val displayText: String) {
    UnknownCommand("Unknown command"),
    InvalidExpression("Invalid expression"),
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
        private val regex = Regex("[a-zA-Z]+]")

        fun tryParse(string: String): Result<Identifier> =
            if (regex.matches(string)) Success(Identifier(string))
            else Failure("Identifier can contain only Latin characters!")
    }
}

sealed interface Input {
    class ArithmeticExpression(val expression: Expression) : Input
    class VariableAssigment(val assignment: Assignment) : Input
    class Command(val type: CommandType) : Input
    class Error(val type: ErrorType) : Input
    object Empty : Input
}
