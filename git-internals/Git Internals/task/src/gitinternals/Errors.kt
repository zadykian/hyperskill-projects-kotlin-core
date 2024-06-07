package gitinternals

sealed class Error(val displayText: CharSequence) {
    data object UnknownCommand : Error("Unknown command")
    data object InvalidDirectoryPath : Error("Specified path is invalid!")
    data object DirectoryNotFound : Error("Specified directory does not exist!")

    class InvalidGitBranches(message: CharSequence) : Error(message)

    open class FailedToReadGitObject(message: CharSequence) : Error(message)
    class UnknownGitObjectType(actual: CharSequence) : FailedToReadGitObject("Unknown git object type '$actual'")
    class FailedToReadGitBranch(message: CharSequence) : Error(message)

    open class ParsingFailed(message: CharSequence) : Error(message)
    data object InvalidGitObjectHash :
        ParsingFailed("Invalid git object hash! Should be either SHA-1 (40 hex digits) or SHA-256 (64 hex digits).")
}
