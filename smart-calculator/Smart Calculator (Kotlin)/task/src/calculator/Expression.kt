package calculator

enum class Operator {
    Plus,
    Minus,
}

sealed interface Expression {
    class Number(val value: UInt) : Expression
    class Unary(val opertr: Operator, val operand: Expression) : Expression
    class Binary(val opertr: Operator, val left: Expression, val right: Expression) : Expression
}
