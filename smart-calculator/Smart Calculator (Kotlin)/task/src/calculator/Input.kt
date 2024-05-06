package calculator

enum class CommandType {
    Help,
    Exit,
}

sealed interface Input {
    class Expression(val expression: calculator.Expression) : Input
    class Assignment(val identifier: Identifier, val expression: calculator.Expression) : Input
    class Command(val type: CommandType) : Input
    data object Empty : Input
}
