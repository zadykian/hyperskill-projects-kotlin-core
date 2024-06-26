package calculator

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import calculator.parser.Errors
import calculator.parser.ParserError

class Identifier private constructor(private val value: String) {
    override fun toString() = value

    override fun hashCode() = value.hashCode()

    override fun equals(other: Any?) = when {
        this === other -> true
        other is Identifier -> value == other.value
        else -> false
    }

    companion object {
        private val regex = Regex("^[a-zA-Z]+$")

        fun tryParse(string: String): Either<ParserError, Identifier> =
            if (regex.matches(string)) Identifier(string).right()
            else Errors.invalidIdentifier().left()
    }
}
