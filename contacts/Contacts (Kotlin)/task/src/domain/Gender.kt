package contacts.domain

import arrow.core.left
import arrow.core.right
import contacts.Error

enum class Gender(val displayName: String) {
    Male("M"),

    Female("F");

    companion object {
        private val namesToValues = entries.associateBy { it.displayName }

        operator fun invoke(value: String) =
            try {
                valueOf(value).right()
            } catch (e: Exception) {
                namesToValues[value]?.right() ?: Error.InvalidInput("Invalid gender string: '$value'").left()
            }
    }
}
