import arrow.core.left
import arrow.core.raise.either
import assertk.assertThat
import assertk.assertions.isEqualTo
import calculator.Calculator
import calculator.parser.Errors
import calculator.parser.ExpressionCommandParser
import calculator.parser.Token.*
import calculator.parser.Token.Number
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class CalculationTests {
    @ParameterizedTest
    @MethodSource("negativeTestCases")
    fun failWithCorrectError(testCase: Failure) {
        val invocationResult = either {
            val command = ExpressionCommandParser.parse(testCase.input).bind()
            Calculator().evaluate(command.expression).bind()
        }

        assertThat(invocationResult).isEqualTo(testCase.expected.left())
    }

    companion object {
        @JvmStatic
        fun negativeTestCases(): List<Failure> = buildList {
            addAll(generalNegative())
            addAll(invalidSequentialOperators())
            addAll(invalidSingleToken())
            addAll(binaryOpsWithoutSecondOperand())
        }

        private fun generalNegative(): Sequence<Failure> = sequenceOf(
            // 1 2
            Failure(input = listOf(Number(1), Number(2)), expected = Errors.invalidExpression()),
            // 1 ( 2 )
            Failure(
                input = listOf(Number(1), OpeningParen, Number(2), ClosingParen),
                expected = Errors.invalidExpression()
            ),
            // 1 + * 2
            Failure(input = listOf(Number(1), Plus, Asterisk, Number(2)), expected = Errors.invalidExpression()),
            // 1 + ( 2 ) )
            Failure(
                input = listOf(Number(1), Plus, OpeningParen, Number(2)),
                expected = Errors.unbalancedParens()
            ),
        )

        private fun invalidSequentialOperators(): Sequence<Failure> {
            val operators = sequenceOf(Asterisk, Slash, Attic)
            val pairs = operators.flatMap { left -> operators.map { right -> arrayOf(left, right) } }
            return pairs.map { Failure(listOf(Number(1), *it, Number(2)), Errors.invalidExpression()) }
        }

        private fun invalidSingleToken(): Sequence<Failure> {
            val operators = sequenceOf(Plus, Minus, Asterisk, Slash, Attic, Equals, OpeningParen, ClosingParen)
            return operators.map { Failure(listOf(it), Errors.invalidExpression()) }
        }

        private fun binaryOpsWithoutSecondOperand() =
            sequenceOf(Asterisk, Slash, Attic)
                .flatMap { sequenceOf(listOf(it, Number(1)), listOf(Number(1), it)) }
                .plus(element = listOf(Number(1), Plus))
                .plus(element = listOf(Number(1), Minus))
                .map { Failure(it, Errors.invalidExpression()) }
    }
}