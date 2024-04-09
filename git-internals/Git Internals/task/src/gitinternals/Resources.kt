package gitinternals

import java.nio.file.Path

object Requests {
    const val GIT_OBJECT_LOCATION = "Enter git object location:"
}

object Errors {
    const val INVALID_PATH = "Invalid path!"
    fun fileDoesntExist(path: Path) = "File does not exist: $path"
}