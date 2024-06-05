package gitinternals

import arrow.core.raise.Raise
import arrow.core.raise.ensure
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class Application(private val io: IO) {
    fun run() {

    }

    context(Raise<Error.DirectoryNotFound>)
    private fun getGitRootDirectory(): Path {
        io.write(Requests.GIT_ROOT_DIRECTORY)
        val path = Paths.get(io.read())
        ensure(Files.exists(path)) { Error.DirectoryNotFound }
        return path
    }

    context(Raise<Error.InvalidGitObjectHash>)
    private fun getGitObjectHash() {
        io.write(Requests.GIT_OBJECT_HASH)
        val objectHash = io.read()
    }
}