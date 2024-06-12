package contacts.domain

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

interface PhoneBook {
    fun add(record: Record<*>)

    context(RaiseInvalidInput)
    fun replace(oldRecord: Record<*>, newRecord: Record<*>): PhoneBookEntry

    context(RaiseInvalidInput)
    fun remove(record: Record<*>)

    fun find(query: NonEmptyString): List<PhoneBookEntry>
    fun listAll(): List<PhoneBookEntry>
}
