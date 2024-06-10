import arrow.core.Either
import assertk.assertThat
import assertk.assertions.isInstanceOf
import contacts.PhoneNumber
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

data class PhoneNumberTestCase(val input: String, val isValid: Boolean)
private typealias Case = PhoneNumberTestCase

class PhoneNumberTests {
    @ParameterizedTest
    @MethodSource("testCases")
    fun `Create a new phone number from string`(testCase: Case) {
        val numberCreated = PhoneNumber(testCase.input)
        val expected = if (testCase.isValid) Either.Right::class else Either.Left::class
        assertThat(numberCreated).isInstanceOf(expected)
    }

    companion object {
        @JvmStatic
        fun testCases() = listOf(
            Case("123", true),
            Case("123 abc", true),
            Case("123-ABC", true),
            Case("123 456 xyz", true),
            Case("123-456-XYZ", true),
            Case("123 456-789", true),
            Case("123-456 789", true),
            Case("123 45-up-89", true),

            Case("(123)", true),
            Case("(123) 456", true),
            Case("123-(456)", true),
            Case("123 (456) 789", true),
            Case("123-(456)-789", true),
            Case("(123) 456-789", true),
            Case("(123)-456 789", true),
            Case("123 (45)-67-89", true),
            Case("+(phone)", true),

            Case("123+456 78912", false),
            Case("(123)-456-(78912)", false),
            Case("9", true),
            Case("123 456 9", false),
            Case("123 9 9234", false),
            Case("123 4?5 678", false),
            Case("+(with space)", false),

            Case("193", true),
            Case("129 abf", true),
            Case("123-AFC", true),
            Case("154 456 xyz", true),
            Case("123-566-XYZ", true),
            Case("123 456-349", true),
            Case("134-456 789", true),
            Case("123 45-down-89", true),

            Case("(234)", true),
            Case("(123) 566", true),
            Case("873-(456)", true),
            Case("123 (786) 789", true),
            Case("163-(456)-789", true),
            Case("(123) 496-789", true),
            Case("(173)-456 789", true),
            Case("123 (95)-67-89", true),
            Case("+(another)", true),

            Case("132+456 78912", false),
            Case("(123)-456-(45912)", false),
            Case("8", true),
            Case("153 456 9", false),
            Case("823 9 9234", false),
            Case("123 4?5 654", false),
            Case("+(another space)", false),

            Case("+1 ()", false),
            Case("+1 11", true),
            Case("(123) (234) 345-456", false),
            Case("+0(123)456-789-9999", false),
            Case("", false),
            Case("()()", false),
        )
    }
}
