package contacts

import contacts.dynamic.DisplayName
import contacts.dynamic.DynamicallyInvokable

data class Record @DynamicallyInvokable constructor(
    @DisplayName("name") val name: NonEmptyString,
    @DisplayName("surname") val surname: NonEmptyString,
    @DisplayName("number") val phoneNumber: PhoneNumber?,
) {
    override fun toString() = "$name $surname, ${phoneNumber ?: "[no number]"}"
}

class PhoneBook {
    private val records = mutableListOf<Record>()

    fun add(record: Record) = records.add(record)

    fun remove(record: Record) = records.remove(record)

    fun edit() {
        TODO()
    }

    fun listAll(): List<Record> = records
}
