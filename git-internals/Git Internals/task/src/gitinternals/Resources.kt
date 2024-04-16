package gitinternals

object Requests {
    const val GIT_ROOT_DIRECTORY = "Enter .git directory location:"
    const val GIT_OBJECT_HASH = "Enter git object hash:"
}

object Errors {
    const val DIRECTORY_NOT_FOUND = "Specified directory does not exist!"
    const val INVALID_GIT_OBJECT_HASH = "Invalid git object hash! Should be either SHA-1 (40 hex digits) or SHA-256 (64 hex digits)."
    const val GIT_OBJECT_NOT_FOUND = "Git object with specified hash does not exist!"

    fun invalidGitObjectHeader(actual: String) = "Invalid git object header! Actual value: '$actual;"
}
