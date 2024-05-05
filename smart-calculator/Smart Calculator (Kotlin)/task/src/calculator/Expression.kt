package calculator

enum class Operator {
    Plus,
    Minus,
}

typealias Value = Int

class Assignment(val identifier: Identifier, val expression: Expression)

sealed interface Expression {
    class Number(val value: Value) : Expression
    class Variable(val identifier: Identifier) : Expression
    class Unary(val opertr: Operator, val operand: Expression) : Expression
    class Binary(val opertr: Operator, val left: Expression, val right: Expression) : Expression
}
