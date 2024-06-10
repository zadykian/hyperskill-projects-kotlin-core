package contacts.domain

import arrow.core.raise.ensure
import contacts.Error
import contacts.RaiseInvalidInput

data class PhoneBookEntry(val record: Record, val info: RecordInfo)

class PhoneBook {
    private val entries = mutableListOf<PhoneBookEntry>()

    fun add(record: Record) {
        val entry = PhoneBookEntry(record, RecordInfo.newCreated())
        entries.add(entry)
    }

    context(RaiseInvalidInput)
    fun remove(record: Record) {
        val wasRemoved = entries.removeIf { it.record == record }
        ensure(wasRemoved) { Errors.recordDoesNotExist(record) }
    }

    context(RaiseInvalidInput)
    fun replace(oldRecord: Record, newRecord: Record) {
        val oldEntry = entries.find { it.record == oldRecord }
        ensure(oldEntry != null) { Errors.recordDoesNotExist(oldRecord) }
        val index = entries.indexOf(oldEntry)

        val newEntry = PhoneBookEntry(newRecord, oldEntry.info.updatedNow())
        entries[index] = newEntry
    }

    fun listAll(): List<PhoneBookEntry> = entries

    private object Errors {
        fun recordDoesNotExist(record: Record) = Error.InvalidInput("Record '$record' doesn't exist in the Phone Book!")
    }
}
