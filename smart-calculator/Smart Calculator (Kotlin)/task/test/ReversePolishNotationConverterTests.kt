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
                input = listOf(
                    Number(1)
                ),
                expected = listOf(
                    Num(1)
                ).right()
            ),
            PolishTestCase(
                input = listOf(
                    Minus, Number(1)
                ),
                expected = listOf(
                    Num(1), Op(Unary.Negation)
                ).right()
            ),
            PolishTestCase(
                // 1 + 2
                input = listOf(
                    Number(1), Plus, Number(2)
                ),
                // 1 2 +
                expected = listOf(
                    Num(1), Num(2), Op(Binary.Add),
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