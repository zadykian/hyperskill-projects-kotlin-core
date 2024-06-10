import arrow.core.Ior
import assertk.Assert
import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.prop
import assertk.assertions.support.expected
import contacts.Error
import contacts.Record
import contacts.dynamic.ObjectInitializer
import contacts.toNonEmpty
import org.junit.jupiter.api.Test

class ObjectInitializerTests {
    @Test
    fun `Create new Record based on valid input`() {
        val result = ObjectInitializer.createNew<Record> {
            when (it) {
                "name" -> "SomeName"
                "surname" -> "SomeSurname"
                "number" -> "+0 (123) 456 789"
                else -> throw IllegalArgumentException("Unknown property: $it")
            }
        }

        fun Assert<Ior<Error, Record>>.isRight() = given {
            when (it) {
                is Ior.Right -> return@given
                is Ior.Left -> expected("Object creation failed: ${it.value.displayText}")
                is Ior.Both -> expected("Object creation is completed with warnings: ${it.leftValue.displayText}")
            }
        }

        assertThat(result).isRight()

        val record = result.getOrNull()!!
        assertThat(record.name.toString()).isEqualTo("SomeName")
        assertThat(record.surname.toString()).isEqualTo("SomeSurname")
        assertThat(record.phoneNumber.toString()).isEqualTo("+0 (123) 456 789")
    }

    @Test
    fun `Create new Record with invalid phone number (Optional)`() {
        val result = ObjectInitializer.createNew<Record> {
            when (it) {
                "name" -> "SomeName"
                "surname" -> "SomeSurname"
                "number" -> "INVALID_PHONE_NUMBER"
                else -> throw IllegalArgumentException("Unknown property: $it")
            }
        }

        fun Assert<Ior<Error, Record>>.isBoth() = given {
            when (it) {
                is Ior.Both -> {
                    assertThat(it.leftValue.displayText).isEqualTo("Wrong number format!")
                    assertThat(it.rightValue).all {
                        prop(Record::name).isEqualTo("SomeName".toNonEmpty())
                        prop(Record::surname).isEqualTo("SomeSurname".toNonEmpty())
                        prop(Record::phoneNumber).isNull()
                    }
                }

                is Ior.Right -> expected("Phone number warning is missing")
                is Ior.Left -> expected("Object creation failed: ${it.value.displayText}")
            }
        }

        assertThat(result).isBoth()
    }

    @Test
    fun `Fail to create new Record because of invalid name`() {
        val result = ObjectInitializer.createNew<Record> {
            when (it) {
                "name" -> ""
                "surname" -> "SomeSurname"
                "number" -> "+0 (123) 456 789"
                else -> throw IllegalArgumentException("Unknown property: $it")
            }
        }

        fun Assert<Ior<Error, Record>>.isLeft() = given {
            when (it) {
                is Ior.Right, is Ior.Both -> expected("Record creation is expected to fail")
                is Ior.Left -> assertThat(it.value.displayText).isEqualTo("Value cannot be empty")
            }
        }

        assertThat(result).isLeft()
    }
}
