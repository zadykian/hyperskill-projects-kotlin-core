package gitinternals

import java.io.BufferedReader
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.Paths

fun tryGetPath(path: String): Result<Path> =
    try {
        Success(Paths.get(path))
    } catch (e: InvalidPathException) {
        Failure(Errors.INVALID_PATH)
    }

fun BufferedReader.readWhile(predicate: (Char) -> Boolean): Sequence<Char> = sequence {
    while (true) {
        val char = read()
        if (char == -1 || !predicate(char.toChar())) {
            break
        }
        yield(char.toChar())
    }
}
