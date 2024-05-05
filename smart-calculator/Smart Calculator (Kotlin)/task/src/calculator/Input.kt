package calculator

enum class CommandType {
    Help,
    Exit,
}

enum class ErrorType(val displayText: String) {
    UnknownCommand("Unknown command"),
    InvalidExpression("Invalid expression"),
}

sealed interface Input {
    class ArithmeticOperation(val expression: Expression) : Input
    class Command(val type: CommandType) : Input
    class Error(val type: ErrorType) : Input
    object Empty : Input
}
