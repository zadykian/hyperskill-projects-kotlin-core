package contacts

data class Record(
    @DisplayName("name")
    val name: NonEmptyString,
    @DisplayName("surname")
    val surname: NonEmptyString,
    @DisplayName("number")
    val phoneNumber: PhoneNumber?,
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
