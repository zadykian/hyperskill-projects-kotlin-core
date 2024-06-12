package contacts.domain

import arrow.core.raise.ensure
import contacts.Error
import contacts.RaiseInvalidInput

data class PhoneBookEntry(val record: Record<*>, val createdAt: DateTimeSafe, val updatedAt: DateTimeSafe) {
    override fun toString() = buildString {
        append(record)
        append("Time created: ").appendLine(createdAt)
        append("Time last edit: ").appendLine(updatedAt)
    }
}

class PhoneBook {
    private val entries = mutableListOf<PhoneBookEntry>()

    fun add(record: Record<*>) {
        val entry = PhoneBookEntry(record, createdAt = DateTimeSafe.now(), updatedAt = DateTimeSafe.now())
        entries.add(entry)
    }

    context(RaiseInvalidInput)
    fun remove(record: Record<*>) {
        val wasRemoved = entries.removeIf { it.record == record }
        ensure(wasRemoved) { Errors.recordDoesNotExist(record) }
    }

    context(RaiseInvalidInput)
    fun replace(oldRecord: Record<*>, newRecord: Record<*>) {
        val oldEntry = entries.find { it.record == oldRecord }
        ensure(oldEntry != null) { Errors.recordDoesNotExist(oldRecord) }
        val index = entries.indexOf(oldEntry)

        val newEntry = oldEntry.copy(record = newRecord, updatedAt = DateTimeSafe.now())
        entries[index] = newEntry
    }

    fun listAll(): List<PhoneBookEntry> = entries

    private object Errors {
        fun recordDoesNotExist(record: Record<*>) =
            Error.InvalidInput("Record '${record.toStringShort()}' doesn't exist in the Phone Book!")
    }
}
