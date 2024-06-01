import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import calculator.Operator.Binary
import calculator.Operator.Unary
import calculator.parser.*
import calculator.parser.PostfixTerm.Num
import calculator.parser.PostfixTerm.Op
import calculator.parser.Token.*
import calculator.parser.Token.Number
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

data class PolishTestCase(
    val input: List<Token>,
    val expected: Either<ParseError, List<PostfixTerm>>
) {
    override fun toString(): String = input.joinToString(separator = " ")
}

class ReversePolishNotationConverterTests {
    @ParameterizedTest
    @MethodSource("testCases")
    fun `Convert from infix to postfix notation`(testCase: PolishTestCase) {
        val actual = ReversePolishNotationConverter.convertFromInfixToPostfix(testCase.input)
        assertThat(actual).isEqualTo(testCase.expected)
    }

    companion object {
        @JvmStatic
        fun testCases(): List<PolishTestCase> = buildList {
            addAll(positiveTestCases())
            addAll(negativeTestCases())
            addAll(invalidSequentialOpsTestCases())
        }

        private fun positiveTestCases() = sequenceOf(
            PolishTestCase(
                // 1
                input = listOf(Number(1)),
                // 1
                expected = listOf(Num(1)).right()
            ),
            PolishTestCase(
                // (1)
                input = listOf(OpeningParen, Number(1), ClosingParen),
                // 1
                expected = listOf(Num(1)).right()
            ),
            PolishTestCase(
                // +1
                input = listOf(Plus, Number(1)),
                // 1 +
                expected = listOf(Num(1), Op(Unary.Plus)).right()
            ),
            PolishTestCase(
                // (+1)
                input = listOf(OpeningParen, Plus, Number(1), ClosingParen),
                // 1 +
                expected = listOf(Num(1), Op(Unary.Plus)).right()
            ),
            PolishTestCase(
                // ++1
                input = listOf(Plus, Plus, Number(1)),
                // 1 + +
                expected = listOf(Num(1), Op(Unary.Plus), Op(Unary.Plus)).right()
            ),
            PolishTestCase(
                // -1
                input = listOf(Minus, Number(1)),
                // 1 -
                expected = listOf(Num(1), Op(Unary.Negate)).right()
            ),
            PolishTestCase(
                // 1 + 2
                input = listOf(Number(1), Plus, Number(2)),
                // 1 2 +
                expected = listOf(Num(1), Num(2), Op(Binary.Add)).right()
            ),
            PolishTestCase(
                // 1 +++ 2
                input = listOf(Number(1), Plus, Plus, Plus, Number(2)),
                // 1 2 u+ u+ b+
                expected = listOf(Num(1), Num(2), Op(Unary.Plus), Op(Unary.Plus), Op(Binary.Add)).right()
            ),
            PolishTestCase(
                // 1 + -2
                input = listOf(Number(1), Plus, Minus, Number(2)),
                // 1 2 - +
                expected = listOf(Num(1), Num(2), Op(Unary.Negate), Op(Binary.Add)).right()
            ),
            PolishTestCase(
                // 1 ^ -2
                input = listOf(Number(1), Attic, Minus, Number(2)),
                // 1 2 - ^
                expected = listOf(Num(1), Num(2), Op(Unary.Negate), Op(Binary.Power)).right()
            ),
            PolishTestCase(
                // 1 + 2 * 3
                input = listOf(Number(1), Plus, Number(2), Asterisk, Number(3)),
                // 1 2 3 * +
                expected = listOf(Num(1), Num(2), Num(3), Op(Binary.Multiply), Op(Binary.Add)).right()
            ),
            PolishTestCase(
                // 1 * 2 / 3
                input = listOf(Number(1), Asterisk, Number(2), Slash, Number(3)),
                // 1 2 * 3 /
                expected = listOf(Num(1), Num(2), Op(Binary.Multiply), Num(3), Op(Binary.Divide)).right()
            ),
            PolishTestCase(
                // 1 ^ 2 * 3
                input = listOf(Number(1), Attic, Number(2), Asterisk, Number(3)),
                // 1 2 ^ 3 *
                expected = listOf(Num(1), Num(2), Op(Binary.Power), Num(3), Op(Binary.Multiply)).right()
            ),
            PolishTestCase(
                // 2 * (3 + 4) + 1
                input = listOf(
                    Number(2), Asterisk, OpeningParen, Number(3), Plus, Number(4), ClosingParen, Plus, Number(1)
                ),
                // 2 3 4 + * 1 +
                expected = listOf(
                    Num(2), Num(3), Num(4), Op(Binary.Add), Op(Binary.Multiply), Num(1), Op(Binary.Add),
                ).right()
            ),
        )

        private fun negativeTestCases() = sequenceOf(
            PolishTestCase(
                // 1 2
                input = listOf(Number(1), Number(2)),
                expected = Errors.INVALID_EXPRESSION.left()
            ),
            PolishTestCase(
                // 1 + * 2
                input = listOf(Number(1), Plus, Asterisk, Number(2)),
                expected = Errors.INVALID_EXPRESSION.left()
            ),
            PolishTestCase(
                // 1 ( 2 )
                input = listOf(Number(1), OpeningParen, Number(2), ClosingParen),
                expected = Errors.INVALID_EXPRESSION.left()
            ),
            PolishTestCase(
                // (
                input = listOf(OpeningParen),
                expected = Errors.UNBALANCED_PARENS_IN_EXPRESSION.left()
            ),
            PolishTestCase(
                // )
                input = listOf(ClosingParen),
                expected = Errors.UNBALANCED_PARENS_IN_EXPRESSION.left()
            ),
            PolishTestCase(
                // 1 + ( 2 ) )
                input = listOf(Number(1), Plus, OpeningParen, Number(2)),
                expected = Errors.UNBALANCED_PARENS_IN_EXPRESSION.left()
            ),
        )

        private fun invalidSequentialOpsTestCases(): Sequence<PolishTestCase> {
            val operators = sequenceOf(Asterisk, Slash, Attic)
            val pairs = operators.flatMap { left -> operators.map { right -> arrayOf(left, right) } }
            return pairs.map { PolishTestCase(listOf(Number(1), *it, Number(2)), Errors.INVALID_EXPRESSION.left()) }
        }
    }
}