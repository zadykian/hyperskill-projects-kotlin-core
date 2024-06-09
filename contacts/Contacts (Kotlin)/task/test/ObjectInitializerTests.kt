import arrow.core.Either
import arrow.core.raise.either
import assertk.Assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.support.expected
import contacts.Error
import contacts.ObjectInitializer
import contacts.Record
import org.junit.jupiter.api.Test

class ObjectInitializerTests {
    @Test
    fun createNewRecord() {
        val result = either {
            ObjectInitializer.createNew<Record> {
                when (it) {
                    "name" -> "SomeName"
                    "surname" -> "SomeSurname"
                    "number" -> "+0 (123) 456 789"
                    else -> throw IllegalArgumentException("Unknown property: $it")
                }
            }
        }

        assertThat(result).isRight()

        val record = result.getOrNull()!!
        assertThat(record.name.toString()).isEqualTo("SomeName")
        assertThat(record.surname.toString()).isEqualTo("SomeSurname")
        assertThat(record.phoneNumber.toString()).isEqualTo("+0 (123) 456 789")
    }

    private fun Assert<Either<Error, Record>>.isRight() = given {
        when (it) {
            is Either.Right -> return@given
            is Either.Left -> expected("Object creation failed: ${it.value.displayText}")
        }
    }
}