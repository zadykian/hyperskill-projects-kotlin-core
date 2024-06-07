package gitinternals

sealed class Error(val displayText: CharSequence) {
    open class InvalidInput(displayText: CharSequence) : Error(displayText)
    data object UnknownCommand : InvalidInput("Unknown command")
    data object InvalidDirectoryPath : InvalidInput("Specified path is invalid!")
    data object DirectoryNotFound : InvalidInput("Specified directory does not exist!")

    open class FailedToReadGitObject(message: CharSequence) : Error(message)
    class UnknownGitObjectType(actual: CharSequence) : FailedToReadGitObject("Unknown git object type '$actual'")

    open class FailedToReadGitBranch(message: CharSequence) : Error(message)
    class InvalidGitBranches(message: CharSequence) : FailedToReadGitBranch(message)
    data object GitBranchNotFound : FailedToReadGitBranch("Git branch is not found")

    open class ParsingFailed(message: CharSequence) : Error(message)
    data object InvalidGitObjectHash :
        ParsingFailed("Invalid git object hash! Should be either SHA-1 (40 hex digits) or SHA-256 (64 hex digits).")
}
