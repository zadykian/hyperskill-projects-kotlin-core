package calculator

import java.math.BigInteger

typealias Value = BigInteger

enum class Associativity {
    Left,
    Right
}

sealed interface Operator {
    val precedence: Byte
    val associativity: Associativity

    sealed class Unary(override val precedence: Byte, override val associativity: Associativity) : Operator {
        data object Plus : Unary(2, Associativity.Right)
        data object Negate : Unary(2, Associativity.Right)
    }

    sealed class Binary(override val precedence: Byte, override val associativity: Associativity) : Operator {
        data object Add : Binary(0, Associativity.Left)
        data object Subtract : Binary(0, Associativity.Left)
        data object Multiply : Binary(1, Associativity.Left)
        data object Divide : Binary(1, Associativity.Left)
        data object Power : Binary(2, Associativity.Right)
    }
}

sealed interface ExpressionTerm {
    @JvmInline
    value class Num(val value: Value) : ExpressionTerm {
        constructor(value: Int) : this(value.toBigInteger())
    }

    @JvmInline
    value class Id(val value: Identifier) : ExpressionTerm

    @JvmInline
    value class Op(val value: Operator) : ExpressionTerm
}

data class Expression(val postfixTerms: List<ExpressionTerm>)
