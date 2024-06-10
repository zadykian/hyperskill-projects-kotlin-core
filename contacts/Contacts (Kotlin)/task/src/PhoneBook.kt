package contacts

import arrow.core.raise.ensure
import contacts.dynamic.annotations.DisplayName
import contacts.dynamic.annotations.DynamicallyInvokable
import contacts.dynamic.annotations.Optional

data class Record @DynamicallyInvokable constructor(
    @DisplayName("name") val name: NonEmptyString,
    @DisplayName("surname") val surname: NonEmptyString,
    @DisplayName("number") @Optional val phoneNumber: PhoneNumber?,
) {
    override fun toString() = "$name $surname, ${phoneNumber ?: "[no number]"}"
}

class PhoneBook {
    private val records = mutableListOf<Record>()

    fun add(record: Record) = records.add(record)

    context(RaiseInvalidInput)
    fun remove(record: Record) {
        val wasRemoved = records.remove(record)
        ensure(wasRemoved) { Errors.recordDoesNotExist(record) }
    }

    context(RaiseInvalidInput)
    fun edit(existingRecord: Record, modification: (Record) -> Record) {
        val index = records.indexOf(existingRecord)
        ensure(index > 0) { Errors.recordDoesNotExist(existingRecord) }
        val modified = modification(records[index])
        records[index] = modified
    }

    fun listAll(): List<Record> = records

    private object Errors {
        fun recordDoesNotExist(record: Record) = Error.InvalidInput("Record '$record' doesn't exist in the Phone Book!")
    }
}
