package contacts.domain

import arrow.core.raise.ensure
import contacts.Error
import contacts.RaiseInvalidInput
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class PhoneBookEntry(val record: Record<*>, val createdAt: LocalDateTime, val updatedAt: LocalDateTime) {
    override fun toString() = buildString {
        append(record)
        append("Time created: ").appendLine(dateTimeFormatter.format(createdAt))
        append("Time last edit: ").append(dateTimeFormatter.format(updatedAt))
    }

    companion object {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    }
}

class PhoneBook {
    private val entries = mutableListOf<PhoneBookEntry>()

    fun add(record: Record<*>) {
        val entry = PhoneBookEntry(record, createdAt = LocalDateTime.now(), updatedAt = LocalDateTime.now())
        entries.add(entry)
    }

    context(RaiseInvalidInput)
    fun remove(record: Record<*>) {
        val wasRemoved = entries.removeIf { it.record == record }
        ensure(wasRemoved) { Errors.recordDoesNotExist(record) }
    }

    context(RaiseInvalidInput)
    fun replace(oldRecord: Record<*>, newRecord: Record<*>): PhoneBookEntry {
        val oldEntry = entries.find { it.record == oldRecord }
        ensure(oldEntry != null) { Errors.recordDoesNotExist(oldRecord) }
        val index = entries.indexOf(oldEntry)

        val newEntry = oldEntry.copy(record = newRecord, updatedAt = LocalDateTime.now())
        entries[index] = newEntry
        return newEntry
    }

    fun find(query: NonEmptyString): List<PhoneBookEntry> {
        TODO()
    }

    fun listAll(): List<PhoneBookEntry> = entries

    private object Errors {
        fun recordDoesNotExist(record: Record<*>) =
            Error.InvalidInput("Record '${record.toStringShort()}' doesn't exist in the Phone Book!")
    }
}
