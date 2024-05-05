package calculator

sealed interface Result<out T>

class Failure<out T>(val errorText: String) : Result<T>

class Success<out T>(val value: T) : Result<T>

fun <T, U> Result<T>.bind(transform: (T) -> Result<U>): Result<U> =
    when (this) {
        is Success -> transform(value)
        is Failure -> Failure(errorText)
    }

fun <T, U> Result<T>.map(transform: (T) -> U): Result<U> =
    when (this) {
        is Success -> Success(transform(value))
        is Failure -> Failure(errorText)
    }

fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Success) action(this.value)
    return this
}

fun <T> Result<T>.onFailure(action: (String) -> Unit): Result<T> {
    if (this is Failure) action(this.errorText)
    return this
}
