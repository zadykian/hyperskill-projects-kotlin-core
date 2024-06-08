package gitinternals

import arrow.core.raise.Raise
import gitinternals.Error.*

typealias RaiseAnyError = Raise<Error>
typealias RaiseInvalidInput = Raise<InvalidInput>
typealias RaiseFailedToReadGitObject = Raise<FailedToReadGitObject>
typealias RaiseFailedToReadGitBranch = Raise<FailedToReadGitBranch>
typealias RaiseDeserializationFailed = Raise<DeserializationFailed>

sealed class Error(val displayText: CharSequence) {
    open class InvalidInput(displayText: CharSequence) : Error(displayText)
    open class FailedToReadGitObject(message: CharSequence) : Error(message)
    open class FailedToReadGitBranch(message: CharSequence) : Error(message)
    open class DeserializationFailed(message: CharSequence) : Error(message)
}
