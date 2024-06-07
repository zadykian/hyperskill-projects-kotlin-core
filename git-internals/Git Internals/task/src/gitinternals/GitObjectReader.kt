package gitinternals

import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.core.raise.ensure
import arrow.core.toNonEmptyListOrNull
import gitinternals.parse.GitBlobParser
import gitinternals.parse.GitCommitParser
import gitinternals.parse.GitTreeParser
import gitinternals.parse.RaiseParsingFailed
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.InflaterInputStream

typealias RaiseFailedToReadGitObject = Raise<Error.FailedToReadGitObject>

private data class GitObjectFile(val header: String, val contentLines: NonEmptyList<String>)

object GitObjectReader {
    context(RaiseFailedToReadGitObject, RaiseParsingFailed)
    fun read(gitRootDirectory: Path, gitObjectHash: GitObjectHash): GitObject {
        val gitObjectPath = gitRootDirectory
            .resolve("objects")
            .resolve(gitObjectHash.value.substring(0, 2))
            .resolve(gitObjectHash.value.substring(2))

        this@RaiseFailedToReadGitObject.ensure(Files.exists(gitObjectPath)) {
            Error.FailedToReadGitObject("Git object with specified hash does not exist on disk!")
        }

        return loadObject(gitObjectPath)
    }

    context(RaiseFailedToReadGitObject, RaiseParsingFailed)
    private fun loadObject(gitObjectPath: Path): GitObject {
        val (header, contentLines) = try {
            readFile(gitObjectPath)
        } catch (exception: Exception) {
            raise(Error.FailedToReadGitObject(exception.localizedMessage))
        }

        this@RaiseFailedToReadGitObject.ensure(contentLines.isNotEmpty()) {
            Error.FailedToReadGitObject("Git object file is empty")
        }

        val fileType = header.takeWhile { !it.isWhitespace() }.lowercase()

        val parser = when (fileType) {
            "blob" -> GitBlobParser
            "commit" -> GitCommitParser
            "tree" -> GitTreeParser
            else -> raise(Error.UnknownGitObjectType(header))
        }

        return parser.parse(contentLines)
    }

    context(RaiseFailedToReadGitObject)
    private fun readFile(gitObjectPath: Path): GitObjectFile =
        Files
            .newInputStream(gitObjectPath)
            .let { InflaterInputStream(it) }
            .bufferedReader()
            .use {
                val header = it.readWhile { char -> char != '\u0000' }.joinToString(separator = "")
                val contentLines = it.readLines().toNonEmptyListOrNull()
                    ?: raise(Error.FailedToReadGitObject("File content is not expected to be empty"))
                GitObjectFile(header, contentLines)
            }
}
