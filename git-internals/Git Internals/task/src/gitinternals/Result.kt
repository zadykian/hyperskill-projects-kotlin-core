package gitinternals

sealed interface Result<T>

class Success<T>(val value: T) : Result<T>

class Failure<T>(val errorMessage: String) : Result<T>

fun <T, R> Result<T>.bind(transform: (T) -> Result<R>) = when(this) {
    is Success -> try {
        transform(value)
    } catch (e: Throwable) {
        Failure(e.localizedMessage)
    }
    is Failure -> Failure(this.errorMessage)
}

fun <T> Result<T>.onFailure(handle: (String) -> Unit) = when(this) {
    is Success -> Unit
    is Failure -> handle(this.errorMessage)
}
