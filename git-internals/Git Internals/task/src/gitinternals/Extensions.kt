package gitinternals

import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.Paths

fun tryGetPath(path: String): Path? =
    try {
        Paths.get(path)
    } catch (e: InvalidPathException) {
        null
    }