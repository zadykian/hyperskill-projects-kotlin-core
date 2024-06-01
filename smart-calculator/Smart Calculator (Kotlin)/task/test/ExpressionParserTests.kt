import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import calculator.Expression
import calculator.ExpressionTerm.Num
import calculator.ExpressionTerm.Op
import calculator.Operator.Binary
import calculator.Operator.Unary
import calculator.parser.ExpressionParser
import calculator.parser.Token.*
import calculator.parser.Token.Number
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class ExpressionParserTests {
    @ParameterizedTest
    @MethodSource("positiveTestCases")
    fun `Convert from infix to postfix notation`(testCase: CalcTestCase) {
        val actual = ExpressionParser.parse(testCase.input)
        when (testCase) {
            is Success -> assertThat(actual).isEqualTo(Expression(testCase.expected).right())
            is Failure -> assertThat(actual).isEqualTo(testCase.expected.left())
        }
    }

    companion object {
        @JvmStatic
        fun positiveTestCases(): Iterable<Success> = listOf(
            Success(
                // 1
                input = listOf(Number(1)),
                // 1
                expected = listOf(Num(1))
            ),
            Success(
                // (1)
                input = listOf(OpeningParen, Number(1), ClosingParen),
                // 1
                expected = listOf(Num(1))
            ),
            Success(
                // +1
                input = listOf(Plus, Number(1)),
                // 1 +
                expected = listOf(Num(1), Op(Unary.Plus))
            ),
            Success(
                // (+1)
                input = listOf(OpeningParen, Plus, Number(1), ClosingParen),
                // 1 +
                expected = listOf(Num(1), Op(Unary.Plus))
            ),
            Success(
                // ++1
                input = listOf(Plus, Plus, Number(1)),
                // 1 + +
                expected = listOf(Num(1), Op(Unary.Plus), Op(Unary.Plus))
            ),
            Success(
                // -1
                input = listOf(Minus, Number(1)),
                // 1 -
                expected = listOf(Num(1), Op(Unary.Negate))
            ),
            Success(
                // 1 + 2
                input = listOf(Number(1), Plus, Number(2)),
                // 1 2 +
                expected = listOf(Num(1), Num(2), Op(Binary.Add))
            ),
            Success(
                // 1 +++ 2
                input = listOf(Number(1), Plus, Plus, Plus, Number(2)),
                // 1 2 u+ u+ b+
                expected = listOf(Num(1), Num(2), Op(Unary.Plus), Op(Unary.Plus), Op(Binary.Add))
            ),
            Success(
                // 1 + -2
                input = listOf(Number(1), Plus, Minus, Number(2)),
                // 1 2 - +
                expected = listOf(Num(1), Num(2), Op(Unary.Negate), Op(Binary.Add))
            ),
            Success(
                // 1 ^ -2
                input = listOf(Number(1), Attic, Minus, Number(2)),
                // 1 2 - ^
                expected = listOf(Num(1), Num(2), Op(Unary.Negate), Op(Binary.Power))
            ),
            Success(
                // 1 + 2 * 3
                input = listOf(Number(1), Plus, Number(2), Asterisk, Number(3)),
                // 1 2 3 * +
                expected = listOf(Num(1), Num(2), Num(3), Op(Binary.Multiply), Op(Binary.Add))
            ),
            Success(
                // 1 * 2 / 3
                input = listOf(Number(1), Asterisk, Number(2), Slash, Number(3)),
                // 1 2 * 3 /
                expected = listOf(Num(1), Num(2), Op(Binary.Multiply), Num(3), Op(Binary.Divide))
            ),
            Success(
                // 1 ^ 2 * 3
                input = listOf(Number(1), Attic, Number(2), Asterisk, Number(3)),
                // 1 2 ^ 3 *
                expected = listOf(Num(1), Num(2), Op(Binary.Power), Num(3), Op(Binary.Multiply))
            ),
            Success(
                // 1 ^ 2 ^ 3
                input = listOf(Number(1), Attic, Number(2), Attic, Number(3)),
                // 1 2 3 ^ ^
                expected = listOf(Num(1), Num(2), Num(3), Op(Binary.Power), Op(Binary.Power))
            ),
            Success(
                // 2 * (3 + 4) + 1
                input = listOf(
                    Number(2), Asterisk, OpeningParen, Number(3), Plus, Number(4), ClosingParen, Plus, Number(1)
                ),
                // 2 3 4 + * 1 +
                expected = listOf(
                    Num(2), Num(3), Num(4), Op(Binary.Add), Op(Binary.Multiply), Num(1), Op(Binary.Add),
                )
            ),
        )
    }
}
