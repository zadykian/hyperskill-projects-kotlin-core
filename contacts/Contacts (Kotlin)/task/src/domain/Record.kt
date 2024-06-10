package contacts.domain

import arrow.core.left
import arrow.core.right
import contacts.Error
import contacts.dynamic.annotations.DisplayName
import contacts.dynamic.annotations.DisplayOrder
import contacts.dynamic.annotations.DynamicallyInvokable

sealed interface Record {
    val phoneNumber: PhoneNumber?
    fun toStringShort(): String
}

data class RecordInfo @DynamicallyInvokable constructor(
    @DisplayName("time created") val createdAt: DateTimeSafe,
    @DisplayName("time last edit") val updatedAt: DateTimeSafe,
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
        private val namesToValues = entries.associateBy {
            it.declaringJavaClass.getField(it.name).getAnnotation(DisplayName::class.java).name
        }

        @DynamicallyInvokable
        operator fun invoke(value: String) =
            try {
                valueOf(value).right()
            } catch (e: Exception) {
                namesToValues[value]?.right() ?: Error.InvalidInput("Invalid gender string: '$value'").left()
            }
    }
}

@Suppress("unused")
@DisplayName("person")
@DisplayOrder(1)
data class Person @DynamicallyInvokable constructor(
    @DisplayName("name") val name: NonEmptyString,
    @DisplayName("surname") val surname: NonEmptyString,
    @DisplayName("birth date") val birthDate: DateSafe? = null,
    @DisplayName("gender") val gender: Gender? = null,
    @DisplayName("number") override val phoneNumber: PhoneNumber? = null,
) : Record {
    override fun toStringShort() = "$name $surname"
}

@Suppress("unused")
@DisplayName("organization")
@DisplayOrder(2)
data class Organization @DynamicallyInvokable constructor(
    @DisplayName("organization name") val name: NonEmptyString,
    @DisplayName("address") val address: NonEmptyString?,
    @DisplayName("number") override val phoneNumber: PhoneNumber?,
) : Record {
    override fun toStringShort() = name.toString()
}
