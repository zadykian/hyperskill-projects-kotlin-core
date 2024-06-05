package gitinternals

sealed class Error(val displayText: String) {
    data object DirectoryNotFound : Error("Specified directory does not exist!")
    data object GitObjectNotFound : Error("Git object with specified hash does not exist!")
    data object InvalidGitObjectHash :
        Error("Invalid git object hash! Should be either SHA-1 (40 hex digits) or SHA-256 (64 hex digits).")

    data class InvalidGitObjectHeader(val actual: String) : Error("Invalid git object header! Actual value: '$actual'")
}
