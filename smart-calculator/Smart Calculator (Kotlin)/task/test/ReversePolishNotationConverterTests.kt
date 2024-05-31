import arrow.core.Either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import calculator.Operator
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
                    Number(1), Plus, Number(2)
                ),
                expected = listOf(
                    Num(1), Num(2), Op(Operator.Binary.Addition),
                ).right()
            ),
            PolishTestCase(
                input = listOf(
                    Number(1), Plus, Number(2), Asterisk, Number(3)
                ),
                expected = listOf(
                    Num(1), Num(2), Num(3), Op(Operator.Binary.Multiplication), Op(Operator.Binary.Addition),
                ).right()
            ),
            PolishTestCase(
                input = listOf(OpeningParen, Number(1), Plus, Number(2), ClosingParen, Asterisk, Number(3)),
                expected = listOf(
                    Num(1), Num(2), Op(Operator.Binary.Addition), Num(3), Op(Operator.Binary.Multiplication),
                ).right()
            ),
        )
    }
}