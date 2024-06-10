package contacts

import arrow.core.raise.Raise

typealias RaiseAnyError = Raise<Error>
typealias RaiseInvalidInput = Raise<Error.InvalidInput>
typealias RaiseDynamicInvocationFailed = Raise<Error.DynamicInvocationFailed>

sealed class Error(val displayText: CharSequence) {
    class InvalidInput(displayText: CharSequence) : Error(displayText)
    class DynamicInvocationFailed(displayText: CharSequence) : Error(displayText)
    class Aggregate(errors: List<Error>) : Error(errors.joinToString("\n") { it.displayText })

    companion object {
        fun combine(left: Error, right: Error): Error = Aggregate(listOf(left, right))
    }
}
