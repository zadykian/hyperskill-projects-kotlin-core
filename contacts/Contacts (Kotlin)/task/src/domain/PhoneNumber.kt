package contacts.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import contacts.Error
import org.intellij.lang.annotations.Language

/**
 * Phone number is expected to satisfy the following rules:
 * 1. The phone number should be split into groups using a space or dash. One group is also possible.
 * 2. Before the first group, there may or may not be a plus symbol.
 * 3. The first group or the second group can be wrapped in parentheses,
 *    but there should be no more than one group that is wrapped in parentheses.
 *    There may also be no groups wrapped in parentheses.
 * 4. A group can contain numbers, uppercase, and lowercase English letters.
 *    A group should be at least 2 symbols in length. But the first group may be only one symbol in length.
 */
@JvmInline
value class PhoneNumber private constructor(private val value: String) {
    override fun toString(): String = value

    companion object {
        operator fun invoke(value: String): Either<Error.InvalidInput, PhoneNumber> =
            if (PhoneRegex.pattern.matches(value)) PhoneNumber(value).right()
            else Error.InvalidInput("Wrong number format!").left()
    }

    private object PhoneRegex {
        @Language("RegExp")
        private val symbol = "[a-zA-Z0-9]"

        @Language("RegExp")
        private val firstGroup = "$symbol+"

        @Language("RegExp")
        private val group = "$symbol{2,}"

        @Language("RegExp")
        private val separator = "[-\\s]"

        @Language("RegExp")
        private val start = "\\+?($firstGroup|${firstGroup.inParens()}|($firstGroup$separator${group.inParens()}))"

        val pattern = Regex("^$start($separator$group)*$")

        private fun String.inParens() = "\\($this\\)"
    }
}
