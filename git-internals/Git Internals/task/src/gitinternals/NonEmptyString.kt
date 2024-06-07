package gitinternals

@JvmInline
value class NonEmptyString private constructor(private val value: String) :
    CharSequence by value,
    Comparable<NonEmptyString> {
    init {
        require(value.isNotBlank()) { "Value cannot be blank" }
    }

    override fun compareTo(other: NonEmptyString) = value.compareTo(other.value)

    override fun toString() = value

    companion object {
        operator fun invoke(value: String): NonEmptyString? =
            if (value.isBlank()) null
            else NonEmptyString(value)
    }
}

fun String.toNonEmptyStringOrNull() = NonEmptyString(this)
