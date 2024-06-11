package contacts.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import contacts.Error
import contacts.dynamic.annotations.DynamicallyInvokable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@JvmInline
value class DateSafe private constructor(private val value: LocalDate) {
    override fun toString(): String = dateFormatter.format(value)

    companion object {
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        @DynamicallyInvokable
        operator fun invoke(value: String): Either<Error.InvalidInput, DateSafe> =
            try {
                val date = LocalDate.parse(value, dateFormatter)
                DateSafe(date).right()
            } catch (e: Exception) {
                Error.InvalidInput("Invalid instant string: '$value'").left()
            }
    }
}

@JvmInline
value class DateTimeSafe private constructor(private val value: LocalDateTime) {
    override fun toString(): String = dateTimeFormatter.format(value)

    companion object {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

        @DynamicallyInvokable
        operator fun invoke(value: String): Either<Error.InvalidInput, DateTimeSafe> =
            try {
                val dateTime = LocalDateTime.parse(value, dateTimeFormatter)
                DateTimeSafe(dateTime).right()
            } catch (e: Exception) {
                Error.InvalidInput("Invalid instant string: '$value'").left()
            }

        fun now() = DateTimeSafe(LocalDateTime.now())
    }
}
