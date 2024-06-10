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
            Case("+0 (123) 456-789-ABcd", true),
            Case("(123) 234 345-456", true),
            Case("+0(123)456-789-9999", false),
            Case("", false),
            Case("()()", false),
        )
    }
}