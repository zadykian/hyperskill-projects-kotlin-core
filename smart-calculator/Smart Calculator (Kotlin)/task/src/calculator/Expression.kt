package calculator

typealias Value = Int

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

interface ExpressionTerm {
    @JvmInline
    value class Num(val value: Value) : ExpressionTerm

    @JvmInline
    value class Id(val value: Identifier) : ExpressionTerm

    @JvmInline
    value class Op(val value: Operator) : ExpressionTerm
}

data class Expression(val postfixTerms: List<ExpressionTerm>)
