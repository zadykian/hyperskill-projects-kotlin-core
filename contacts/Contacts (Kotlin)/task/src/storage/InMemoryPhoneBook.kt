package contacts.storage

import arrow.core.raise.ensure
import contacts.Error
import contacts.RaiseInvalidInput
import contacts.domain.NonEmptyString
import contacts.domain.PhoneBook
import contacts.domain.PhoneBookEntry
import contacts.domain.Record
import java.time.LocalDateTime

class InMemoryPhoneBook : PhoneBook {
    private val entries = mutableListOf<PhoneBookEntry>()

    override fun add(record: Record<*>) {
        val entry = PhoneBookEntry(record, createdAt = LocalDateTime.now(), updatedAt = LocalDateTime.now())
        entries.add(entry)
    }

    context(RaiseInvalidInput)
    override fun remove(record: Record<*>) {
        val wasRemoved = entries.removeIf { it.record == record }
        ensure(wasRemoved) { Errors.recordDoesNotExist(record) }
    }

    context(RaiseInvalidInput)
    override fun replace(oldRecord: Record<*>, newRecord: Record<*>): PhoneBookEntry {
        val oldEntry = entries.find { it.record == oldRecord }
        ensure(oldEntry != null) { Errors.recordDoesNotExist(oldRecord) }
        val index = entries.indexOf(oldEntry)

        val newEntry = oldEntry.copy(record = newRecord, updatedAt = LocalDateTime.now())
        entries[index] = newEntry
        return newEntry
    }

    override fun find(query: NonEmptyString): List<PhoneBookEntry> {
        val regex = Regex(".*${query}.*", RegexOption.IGNORE_CASE)
        return entries
            .asSequence()
            .filter { it.record.matches(regex) }
            .toList()
    }

    private fun Record<*>.matches(pattern: Regex) =
        properties.values.filterNotNull().joinToString("") { it.toString() }.matches(pattern)

    override fun listAll(): List<PhoneBookEntry> = entries

    private object Errors {
        fun recordDoesNotExist(record: Record<*>) =
            Error.InvalidInput("Record '${record.toStringShort()}' doesn't exist in the Phone Book!")
    }
}
