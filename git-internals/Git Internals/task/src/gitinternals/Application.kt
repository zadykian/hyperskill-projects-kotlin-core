package gitinternals

import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

data class IO(val read: () -> String, val write: (String) -> Unit)

private typealias RaiseDirectoryNotFound = Raise<Error.DirectoryNotFound>
private typealias RaiseInvalidDirectoryPath = Raise<Error.InvalidDirectoryPath>

class Application(private val io: IO) {
    fun run() {
        getGitObjectHeader()
            .onRight {
                io.write("*${it::class.simpleName!!.uppercase()}*")
                io.write(it.toString())
            }
            .onLeft { io.write(it.displayText) }
    }

    private fun getGitObjectHeader() = either {
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

    context(Raise<Error.InvalidGitObjectHash>)
    private fun getGitObjectHash(): GitObjectHash {
        io.write(Requests.GIT_OBJECT_HASH)
        val objectHashString = io.read()
        return GitObjectHash(objectHashString).bind()
    }
}