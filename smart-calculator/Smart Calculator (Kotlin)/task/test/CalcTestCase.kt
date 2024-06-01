import calculator.ExpressionTerm
import calculator.parser.ParserError
import calculator.parser.Token

sealed class CalcTestCase(val input: List<Token>) {
    override fun toString(): String = input.joinToString(separator = " ")
}

class Success(input: List<Token>, val expected: List<ExpressionTerm>) : CalcTestCase(input)

class Failure(input: List<Token>, val expected: ParserError) : CalcTestCase(input)
