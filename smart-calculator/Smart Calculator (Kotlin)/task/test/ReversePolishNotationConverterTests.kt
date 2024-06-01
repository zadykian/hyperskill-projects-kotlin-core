import arrow.core.Either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import calculator.Operator.Binary
import calculator.Operator.Unary
import calculator.parser.ParseError
import calculator.parser.PostfixTerm
import calculator.parser.PostfixTerm.Num
import calculator.parser.PostfixTerm.Op
import calculator.parser.ReversePolishNotationConverter
import calculator.parser.Token
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
        fun testCases() = listOf(
            PolishTestCase(
                // 1
                input = listOf(
                    Number(1),
                ),
                // 1
                expected = listOf(
                    Num(1),
                ).right()
            ),
            PolishTestCase(
                // +1
                input = listOf(
                    Plus, Number(1),
                ),
                // 1 +
                expected = listOf(
                    Num(1), Op(Unary.Plus),
                ).right()
            ),
            PolishTestCase(
                // ++1
                input = listOf(
                    Plus, Plus, Number(1),
                ),
                // 1 + +
                expected = listOf(
                    Num(1), Op(Unary.Plus), Op(Unary.Plus),
                ).right()
            ),
            PolishTestCase(
                // -1
                input = listOf(
                    Minus, Number(1),
                ),
                // 1 -
                expected = listOf(
                    Num(1), Op(Unary.Negate),
                ).right()
            ),
            PolishTestCase(
                // 1 + 2
                input = listOf(
                    Number(1), Plus, Number(2),
                ),
                // 1 2 +
                expected = listOf(
                    Num(1), Num(2), Op(Binary.Add),
                ).right()
            ),
            PolishTestCase(
                // 1 +++ 2
                input = listOf(
                    Number(1), Plus, Plus, Plus, Number(2)
                ),
                // 1 2 u+ u+ b+
                expected = listOf(
                    Num(1), Num(2), Op(Unary.Plus), Op(Unary.Plus), Op(Binary.Add),
                ).right()
            ),
            PolishTestCase(
                // 1 + -2
                input = listOf(
                    Number(1), Plus, Minus, Number(2)
                ),
                // 1 2 - +
                expected = listOf(
                    Num(1), Num(2), Op(Unary.Negate), Op(Binary.Add),
                ).right()
            ),
            PolishTestCase(
                // 1 ^ -2
                input = listOf(
                    Number(1), Asterisk, Minus, Number(2)
                ),
                // 1 2 - ^
                expected = listOf(
                    Num(1), Num(2), Op(Unary.Negate), Op(Binary.Power),
                ).right()
            ),
            PolishTestCase(
                // 1 + 2 * 3
                input = listOf(
                    Number(1), Plus, Number(2), Asterisk, Number(3)
                ),
                // 1 2 3 * +
                expected = listOf(
                    Num(1), Num(2), Num(3), Op(Binary.Multiply), Op(Binary.Add),
                ).right()
            ),
            PolishTestCase(
                // 1 * 2 / 3
                input = listOf(
                    Number(1), Asterisk, Number(2), Slash, Number(3)
                ),
                // 1 2 * 3 /
                expected = listOf(
                    Num(1), Num(2), Op(Binary.Multiply), Num(3), Op(Binary.Divide),
                ).right()
            ),
            PolishTestCase(
                // 1 ^ 2 * 3
                input = listOf(
                    Number(1), Attic, Number(2), Asterisk, Number(3)
                ),
                // 1 2 ^ 3 *
                expected = listOf(
                    Num(1), Num(2), Op(Binary.Power), Num(3), Op(Binary.Multiply),
                ).right()
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
    }
}