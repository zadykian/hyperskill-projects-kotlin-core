package gitinternals

@JvmInline
value class NonEmptyString private constructor(private val value: String) : CharSequence by value {
    init {
        require(value.isNotBlank()) { "Value cannot be blank" }
    }

    override fun toString() = value

    companion object {
        operator fun invoke(value: String): NonEmptyString? =
            if (value.isBlank()) null
            else NonEmptyString(value)
    }
}

fun String.toNonEmptyStringOrNull() = NonEmptyString(this)

fun Sequence<Char>.toNonEmptyStringOrNull() = NonEmptyString(this.joinToString(separator = ""))
