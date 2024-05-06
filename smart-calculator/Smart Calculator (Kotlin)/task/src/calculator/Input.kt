package calculator

enum class CommandType {
    Help,
    Exit,
}

sealed interface Input {
    class ArithmeticExpression(val expression: Expression) : Input
    class VariableAssigment(val assignment: Assignment) : Input
    class Command(val type: CommandType) : Input
    object Empty : Input
}
