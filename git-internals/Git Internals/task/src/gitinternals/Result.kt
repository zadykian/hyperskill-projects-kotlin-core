package gitinternals

sealed interface Result<T>

class Success<T>(val value: T) : Result<T>

class Failure<T>(val errorMessage: String) : Result<T>
