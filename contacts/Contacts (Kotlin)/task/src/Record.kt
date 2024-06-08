package contacts

data class Record(
    val name: NonEmptyString,
    val surname: NonEmptyString,
    val phone: NonEmptyString,
)