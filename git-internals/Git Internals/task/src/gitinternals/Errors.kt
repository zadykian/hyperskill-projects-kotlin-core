package gitinternals

import arrow.core.raise.Raise
import gitinternals.Error.*

typealias RaiseAnyError = Raise<Error>
typealias RaiseInvalidInput = Raise<InvalidInput>
typealias RaiseFailedToReadGitObject = Raise<FailedToReadGitObject>
typealias RaiseFailedToReadGitBranch = Raise<FailedToReadGitBranch>
typealias RaiseDeserializationFailed = Raise<DeserializationFailed>

sealed class Error(val displayText: CharSequence) {
    class InvalidInput(displayText: CharSequence) : Error(displayText)
    class FailedToReadGitObject(message: CharSequence) : Error(message)
    class FailedToReadGitBranch(message: CharSequence) : Error(message)
    class DeserializationFailed(message: CharSequence) : Error(message)
}
