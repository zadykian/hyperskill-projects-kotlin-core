package gitinternals

import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

data class IO(val read: () -> String, val write: (String) -> Unit)

typealias RaiseDirectoryNotFound = Raise<Error.DirectoryNotFound>
typealias RaiseInvalidDirectoryPath = Raise<Error.InvalidDirectoryPath>
typealias RaiseInvalidGitObjectHash = Raise<Error.InvalidGitObjectHash>

class Application(private val io: IO) {
    fun run() {
        readGitObject()
            .onRight {
                io.write("*${it::class.simpleName!!.uppercase()}*")
                io.write(it.toString())
            }
            .onLeft { io.write(it.displayText) }
    }

    private fun readGitObject() = either {
        val gitRoot = getGitRootDirectory()
        val gitObjectHash = getGitObjectHash()
        GitObjectReader.read(gitRoot, gitObjectHash)
    }

    context(RaiseDirectoryNotFound, RaiseInvalidDirectoryPath)
    private fun getGitRootDirectory(): Path {
        io.write(Requests.GIT_ROOT_DIRECTORY)
        val pathString = io.read()
        val path = try {
            Paths.get(pathString)
        } catch (exception: Exception) {
            raise(Error.InvalidDirectoryPath)
        }
        this@RaiseDirectoryNotFound.ensure(Files.exists(path)) { Error.DirectoryNotFound }
        return path
    }

    context(RaiseInvalidGitObjectHash)
    private fun getGitObjectHash(): GitObjectHash {
        io.write(Requests.GIT_OBJECT_HASH)
        val objectHashString = io.read()
        return GitObjectHash(objectHashString).bind()
    }

    private object Requests {
        const val GIT_ROOT_DIRECTORY = "Enter .git directory location:"
        const val GIT_OBJECT_HASH = "Enter git object hash:"
    }
}
