package gitinternals

sealed class Error(val displayText: String) {
    data object InvalidDirectoryPath : Error("Specified path is invalid!")
    data object DirectoryNotFound : Error("Specified directory does not exist!")
    data object GitObjectNotFound : Error("Git object with specified hash does not exist!")
    data class FailedToReadGitObject(val message: String? = null) : Error(message ?: "Failed to read git object file")
    open class ParsingFailed(message: String? = null) : Error(message ?: "Parsing failed")

    data class InvalidGitObjectHeader(val actual: String) :
        ParsingFailed("Invalid git object header! Actual value: '$actual'")

    data object InvalidGitObjectHash :
        ParsingFailed("Invalid git object hash! Should be either SHA-1 (40 hex digits) or SHA-256 (64 hex digits).")
}
