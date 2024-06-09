package contacts

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import contacts.dynamic.DynamicallyInvokable

@JvmInline
value class NonEmptyString private constructor(private val value: String) :
    CharSequence by value,
    Comparable<NonEmptyString> {

    override fun compareTo(other: NonEmptyString) = value.compareTo(other.value)

    override fun toString() = value

    companion object {
        @DynamicallyInvokable
        operator fun invoke(value: String): Either<Error.InvalidInput, NonEmptyString> =
            if (value.isBlank()) Error.InvalidInput("Value cannot be empty").left()
            else NonEmptyString(value).right()
    }
}

fun String.toNonEmptyOrNull(): NonEmptyString? = NonEmptyString(this).getOrNull()
