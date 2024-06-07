package gitinternals

sealed class Error(val displayText: String) {
    data object InvalidDirectoryPath : Error("Specified path is invalid!")
    data object DirectoryNotFound : Error("Specified directory does not exist!")

    open class FailedToReadGitObject(message: String) : Error(message)
    data class UnknownGitObjectType(val actual: String) : FailedToReadGitObject("Unknown git object type '$actual'")

    open class ParsingFailed(message: String) : Error(message)
    data object InvalidGitObjectHash :
        ParsingFailed("Invalid git object hash! Should be either SHA-1 (40 hex digits) or SHA-256 (64 hex digits).")
}
