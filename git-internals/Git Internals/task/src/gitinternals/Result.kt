package gitinternals

sealed interface Result<T>

class Success<T>(val value: T) : Result<T>

class Failure<T>(val errorMessage: String) : Result<T>

interface IO {
    fun read(): String
    fun write(value: String)

    fun <T> run(scope: IO.() -> Result<T>): Result<T> = scope(this@IO)

    fun <T, R> Result<T>.bind(transform: (T) -> Result<R>) = when(this) {
        is Success -> try {
            transform(value)
        } catch (e: Throwable) {
            Failure(e.localizedMessage)
        }
        is Failure -> Failure(this.errorMessage)
    }

    fun <T> Result<T>.onFailure(handle: IO.(String) -> Unit) = when(this) {
        is Success -> Unit
        is Failure -> handle(this@IO, this@onFailure.errorMessage)
    }
}
