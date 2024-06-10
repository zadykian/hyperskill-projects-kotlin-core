package contacts.domain

import arrow.core.left
import arrow.core.right
import contacts.Error
import contacts.dynamic.annotations.DisplayName
import contacts.dynamic.annotations.DynamicallyInvokable

sealed interface Record {
    val phoneNumber: PhoneNumber?
    fun toStringShort(): String
}

data class RecordInfo(
    @DisplayName("Time created:") val createdAt: DateTimeSafe,
    @DisplayName("Time last edit:") val updatedAt: DateTimeSafe,
) {
    fun updatedNow() = copy(updatedAt = DateTimeSafe.now())

    companion object {
        fun newCreated() = RecordInfo(DateTimeSafe.now(), DateTimeSafe.now())
    }
}

enum class Gender {
    @DisplayName("M")
    Male,

    @DisplayName("F")
    Female;

    companion object {
        @DynamicallyInvokable
        operator fun invoke(value: String) =
            try {
                valueOf(value).right()
            } catch (e: Exception) {
                Error.InvalidInput("Invalid gender string: '$value'").left()
            }
    }
}

@DisplayName("person")
data class Person(
    @DisplayName("name") val name: NonEmptyString,
    @DisplayName("surname") val surname: NonEmptyString,
    @DisplayName("birth date") val birthDate: DateTimeSafe? = null,
    @DisplayName("gender") val gender: Gender? = null,
    @DisplayName("number") override val phoneNumber: PhoneNumber? = null,
) : Record {
    override fun toStringShort() = "$name $surname"
}

@DisplayName("organization")
data class Organization(
    @DisplayName("organization name") val name: NonEmptyString,
    @DisplayName("address") val address: NonEmptyString?,
    @DisplayName("number") override val phoneNumber: PhoneNumber?,
) : Record {
    override fun toStringShort() = name.toString()
}
