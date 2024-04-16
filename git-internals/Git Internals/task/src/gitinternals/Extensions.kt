package gitinternals

import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.Paths

fun tryGetPath(path: String): Result<Path> =
    try {
        Success(Paths.get(path))
    } catch (e: InvalidPathException) {
        Failure(Errors.INVALID_PATH)
    }