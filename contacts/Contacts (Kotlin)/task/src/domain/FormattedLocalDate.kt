package contacts.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import contacts.Error
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@JvmInline
value class FormattedLocalDate private constructor(private val value: LocalDate) {
    override fun toString(): String = dateFormatter.format(value)

    companion object {
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        operator fun invoke(value: String): Either<Error.InvalidInput, FormattedLocalDate> =
            try {
                val date = LocalDate.parse(value, dateFormatter)
                FormattedLocalDate(date).right()
            } catch (e: Exception) {
                Error.InvalidInput("Invalid local date string: '$value' (${e.message})").left()
            }
    }
}
