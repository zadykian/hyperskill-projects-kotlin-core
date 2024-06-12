package contacts.domain

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import contacts.Error
import contacts.RaiseInvalidInput
import contacts.domain.Person.Property

typealias PropertyParser = (String) -> Either<Error.InvalidInput, Any>
typealias Properties<T> = Map<T, Any?>

sealed interface RecordProperty {
    val displayName: String
    val parser: PropertyParser
}

sealed class Record<T : RecordProperty>(val properties: Properties<T>) {
    abstract fun toStringShort(): String

    override fun toString() = buildString {
        properties.forEach {
            val propertyName = it.key.displayName.replaceFirstChar { c -> c.uppercase() }
            appendLine("${propertyName}: ${it.value?.toString() ?: "[no data]"}")
        }
    }
}

class Person private constructor(properties: Properties<Property>) : Record<Property>(properties) {
    override fun toStringShort() = "${properties[Property.Name]} ${properties[Property.Surname]}"

    @Suppress("unused")
    enum class Property(override val displayName: String, override val parser: PropertyParser) : RecordProperty {
        Name("name", { NonEmptyString(it) }),
        Surname("surname", { NonEmptyString(it) }),
        BirthDate("birth date", { DateSafe(it) }),
        Gender("gender", { Gender(it) }),
        PhoneNumber("number", { PhoneNumber(it) }),
    }

    companion object {
        operator fun invoke(properties: Properties<Property>) = either {
            properties.ensureContains(Property.Name, Property.Surname)
            Person(properties)
        }
    }
}

class Organization private constructor(properties: Properties<Property>) : Record<Organization.Property>(properties) {
    override fun toStringShort() = "${properties[Property.Name]}"

    @Suppress("unused")
    enum class Property(override val displayName: String, override val parser: PropertyParser) : RecordProperty {
        Name("organization name", { NonEmptyString(it) }),
        Address("address", { NonEmptyString(it) }),
        PhoneNumber("number", { PhoneNumber(it) }),
    }

    companion object {
        operator fun invoke(properties: Properties<Property>) = either {
            properties.ensureContains(Property.Name)
            Organization(properties)
        }
    }
}

context(RaiseInvalidInput)
private fun <T : RecordProperty> Properties<T>.ensureContains(vararg properties: T) {
    val notProvided = properties.filter { this[it] == null }
    ensure(notProvided.isEmpty()) {
        Error.InvalidInput("Required properties [${notProvided.joinToString()}] are not provided")
    }
}
