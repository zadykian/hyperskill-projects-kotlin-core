import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import calculator.Calculator
import calculator.Value
import calculator.parser.*
import calculator.parser.Token.*
import calculator.parser.Token.Number
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

sealed class CalcTestCase(val input: List<Token>) {
    override fun toString(): String = input.joinToString(separator = " ")
}

class CalcSuccess(input: List<Token>, val expected: Value) : CalcTestCase(input)

class CalcFailure(input: List<Token>, val expected: Error) : CalcTestCase(input)

class CalculationTests {
    @ParameterizedTest
    @MethodSource("positiveTestCases")
    fun parseAndEvaluateExpression(testCase: CalcSuccess) {
        val invocationResult = either {
            val command = ExpressionCommandParser.parse(testCase.input).bind()
            Calculator().evaluate(command.expression).bind()
        }

        assertThat(invocationResult).isEqualTo(testCase.expected.right())
    }

    @ParameterizedTest
    @MethodSource("negativeTestCases")
    fun failWithCorrectError(testCase: CalcFailure) {
        val invocationResult = either {
            val command = ExpressionCommandParser.parse(testCase.input).bind()
            Calculator().evaluate(command.expression).bind()
        }

        assertThat(invocationResult).isEqualTo(testCase.expected.left())
    }

    @Test
    fun evaluateExpressionWithVariables() {
        val assignmentTokens = sequenceOf(
            listOf(Word("a"), Equals, Number(4)),
            listOf(Word("b"), Equals, Number(5)),
            listOf(Word("c"), Equals, Number(6)),
        )

        // a * 2 + b * 3 + c * (2 + 3)
        val expressionTokens = listOf(
            Word("a"), Asterisk, Number(2), Plus,
            Word("b"), Asterisk, Number(3), Plus,
            Word("c"), Asterisk, OpeningParen, Number(2), Plus, Number(3), ClosingParen,
        )

        val calculator = Calculator()

        val result = either {
            assignmentTokens
                .map { AssignmentCommandParser.parse(it).bind() }
                .forEach { calculator.assign(it.identifier, it.expression) }

            val expression = ExpressionParser.parse(expressionTokens).bind()
            calculator.evaluate(expression).bind()
        }

        assertThat(result).isEqualTo(Either.Right(53))
    }

    companion object {
        @JvmStatic
        fun positiveTestCases(): List<CalcSuccess> = listOf(
            CalcSuccess(
                // 1
                input = listOf(Number(1)),
                expected = 1
            ),
            CalcSuccess(
                // -10
                input = listOf(Minus, Number(10)),
                expected = -10
            ),
            CalcSuccess(
                // 1 + 2
                input = listOf(Number(1), Plus, Number(2)),
                expected = 3
            ),
            CalcSuccess(
                // 2 * 2 ^ 3
                input = listOf(Number(2), Asterisk, Number(2), Attic, Number(3)),
                expected = 16
            ),
            CalcSuccess(
                // 2 ^ 3 ^ 2
                input = listOf(Number(2), Attic, Number(3), Attic, Number(2)),
                expected = 512
            ),
            CalcSuccess(
                // 8 * 3 + 12 * (4 - 2)
                input = listOf(
                    Number(8), Asterisk, Number(3), Plus,
                    Number(12), Asterisk, OpeningParen, Number(4), Minus, Number(2), ClosingParen,
                ),
                expected = 48
            ),
            CalcSuccess(
                // 1 +++ 2 * 3 -- 4
                input = listOf(
                    Number(1), Plus, Plus, Plus, Number(2), Asterisk,
                    Number(3), Minus, Minus, Number(4),
                ),
                expected = 11
            ),
        )

        @JvmStatic
        fun negativeTestCases(): List<CalcFailure> = buildList {
            addAll(generalNegative())
            addAll(invalidSequentialOperators())
            addAll(invalidSingleToken())
            addAll(binaryOpsWithoutSecondOperand())
        }

        private fun generalNegative(): Sequence<CalcFailure> = sequenceOf(
            // [empty]
            CalcFailure(input = emptyList(), expected = Errors.emptyInput()),
            // 1 2
            CalcFailure(input = listOf(Number(1), Number(2)), expected = Errors.invalidExpression()),
            // 1 ( 2 )
            CalcFailure(
                input = listOf(Number(1), OpeningParen, Number(2), ClosingParen),
                expected = Errors.invalidExpression()
            ),
            // 1 + * 2
            CalcFailure(input = listOf(Number(1), Plus, Asterisk, Number(2)), expected = Errors.invalidExpression()),
            // 1 2 + +
            CalcFailure(input = listOf(Number(1), Number(2), Plus, Plus), expected = Errors.invalidExpression()),
            // 1 2 * -
            CalcFailure(input = listOf(Number(1), Number(2), Asterisk, Minus), expected = Errors.invalidExpression()),
            // 1 + ( 2 ) )
            CalcFailure(
                input = listOf(Number(1), Plus, OpeningParen, Number(2)),
                expected = Errors.unbalancedParens()
            ),
        )

        private fun invalidSequentialOperators(): Sequence<CalcFailure> {
            val operators = sequenceOf(Asterisk, Slash, Attic)
            val pairs = operators.flatMap { left -> operators.map { right -> arrayOf(left, right) } }
            return pairs.map { CalcFailure(listOf(Number(1), *it, Number(2)), Errors.invalidExpression()) }
        }

        private fun invalidSingleToken(): Sequence<CalcFailure> {
            val operators = sequenceOf(Plus, Minus, Asterisk, Slash, Attic, OpeningParen, ClosingParen)
            return operators.map { CalcFailure(listOf(it), Errors.invalidExpression()) }
        }

        private fun binaryOpsWithoutSecondOperand() =
            sequenceOf(Asterisk, Slash, Attic)
                .flatMap { sequenceOf(listOf(it, Number(1)), listOf(Number(1), it)) }
                .plus(element = listOf(Number(1), Plus))
                .plus(element = listOf(Number(1), Minus))
                .map { CalcFailure(it, Errors.invalidExpression()) }
    }
}