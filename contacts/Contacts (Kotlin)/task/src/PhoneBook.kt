package contacts

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

    fun remove(record: Record) = records.remove(record)

    fun edit() {
        TODO()
    }

    fun listAll(): List<Record> = records
}
