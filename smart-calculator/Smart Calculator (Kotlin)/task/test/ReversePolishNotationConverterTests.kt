import arrow.core.Either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import calculator.Operator
import calculator.parser.ParseError
import calculator.parser.PostfixTerm
import calculator.parser.ReversePolishNotationConverter
import calculator.parser.Token
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
                input = listOf(Token.Number(1)),
                expected = listOf(PostfixTerm.Number(1)).right()
            ),
            PolishTestCase(
                input = listOf(Token.Number(1), Token.Plus, Token.Number(2)),
                expected = listOf(
                    PostfixTerm.Number(1),
                    PostfixTerm.Number(2),
                    PostfixTerm.Operator(Operator.Binary.Addition),
                ).right()
            ),
            PolishTestCase(
                input = listOf(Token.Number(1), Token.Plus, Token.Number(2), Token.Asterisk, Token.Number(3)),
                expected = listOf(
                    PostfixTerm.Number(1),
                    PostfixTerm.Number(2),
                    PostfixTerm.Number(3),
                    PostfixTerm.Operator(Operator.Binary.Multiplication),
                    PostfixTerm.Operator(Operator.Binary.Addition),
                ).right()
            ),
        )
    }
}