import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import calculator.Expression
import calculator.ExpressionTerm
import calculator.ExpressionTerm.Num
import calculator.ExpressionTerm.Op
import calculator.Operator.Binary
import calculator.Operator.Unary
import calculator.parser.Errors
import calculator.parser.ExpressionParser
import calculator.parser.ParseError
import calculator.parser.Token
import calculator.parser.Token.*
import calculator.parser.Token.Number
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

sealed class PolishTestCase(val input: List<Token>) {
    override fun toString(): String = input.joinToString(separator = " ")
}

private class Failure(input: List<Token>, val expected: ParseError) : PolishTestCase(input)
private class Success(input: List<Token>, val expected: List<ExpressionTerm>) : PolishTestCase(input)

class ExpressionParserTests {
    @ParameterizedTest
    @MethodSource("testCases")
    fun `Convert from infix to postfix notation`(testCase: PolishTestCase) {
        val actual = ExpressionParser.parse(testCase.input)
        when (testCase) {
            is Success -> assertThat(actual).isEqualTo(Expression(testCase.expected).right())
            is Failure -> assertThat(actual).isEqualTo(testCase.expected.left())
        }
    }

    companion object {
        @JvmStatic
        fun testCases(): List<PolishTestCase> = buildList {
            addAll(positiveTestCases())
            addAll(negativeTestCases())
            addAll(invalidSequentialOperators())
            addAll(invalidSingleToken())
            addAll(binaryOpsWithoutSecondOperand())
        }

        private fun positiveTestCases(): Sequence<Success> = sequenceOf(
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

        private fun negativeTestCases(): Sequence<Failure> = sequenceOf(
            // 1 2
            Failure(input = listOf(Number(1), Number(2)), expected = Errors.INVALID_EXPRESSION),
            // 1 + * 2
            Failure(input = listOf(Number(1), Plus, Asterisk, Number(2)), expected = Errors.INVALID_EXPRESSION),
            // * 1
            Failure(input = listOf(Asterisk, Number(1)), expected = Errors.INVALID_EXPRESSION),
            // 1 *
            Failure(input = listOf(Number(1), Asterisk), expected = Errors.INVALID_EXPRESSION),
            // 1 ( 2 )
            Failure(
                input = listOf(Number(1), OpeningParen, Number(2), ClosingParen),
                expected = Errors.INVALID_EXPRESSION
            ),
            // 1 + ( 2 ) )
            Failure(
                input = listOf(Number(1), Plus, OpeningParen, Number(2)),
                expected = Errors.UNBALANCED_PARENS_IN_EXPRESSION
            ),
        )

        private fun invalidSequentialOperators(): Sequence<PolishTestCase> {
            val operators = sequenceOf(Asterisk, Slash, Attic)
            val pairs = operators.flatMap { left -> operators.map { right -> arrayOf(left, right) } }
            return pairs.map { Failure(listOf(Number(1), *it, Number(2)), Errors.INVALID_EXPRESSION) }
        }

        private fun invalidSingleToken(): Sequence<PolishTestCase> {
            val operators = sequenceOf(Plus, Minus, Asterisk, Slash, Attic, Equals, OpeningParen, ClosingParen)
            return operators.map { Failure(listOf(it), Errors.INVALID_EXPRESSION) }
        }

        private fun binaryOpsWithoutSecondOperand() =
            sequenceOf(Asterisk, Slash, Attic)
                .flatMap { sequenceOf(listOf(it, Number(1)), listOf(Number(1), it)) }
                .plus(element = listOf(Number(1), Plus))
                .plus(element = listOf(Number(1), Minus))
                .map { Failure(it, Errors.INVALID_EXPRESSION) }
    }
}
