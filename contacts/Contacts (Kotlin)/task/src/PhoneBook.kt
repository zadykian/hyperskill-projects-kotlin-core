package contacts

data class Record(
    val name: NonEmptyString,
    val surname: NonEmptyString,
    val phone: PhoneNumber?,
) {
    override fun toString() = "$name $surname, ${phone ?: "[no number]"}"
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