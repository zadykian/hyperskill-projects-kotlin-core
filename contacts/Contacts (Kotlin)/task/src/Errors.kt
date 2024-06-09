package contacts

import arrow.core.raise.Raise
import contacts.Error.InvalidInput

typealias RaiseAnyError = Raise<Error>
typealias RaiseInvalidInput = Raise<InvalidInput>

sealed class Error(val displayText: CharSequence) {
    class InvalidInput(displayText: CharSequence) : Error(displayText)
    class FailedToCreateObject(displayText: CharSequence) : Error(displayText)
}
