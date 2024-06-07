package gitinternals

import arrow.core.raise.Raise
import arrow.core.raise.ensure
import gitinternals.parse.GitBlobParser
import gitinternals.parse.GitCommitParser
import gitinternals.parse.GitTreeViewParser
import gitinternals.parse.RaiseParsingFailed
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.InflaterInputStream

typealias RaiseFailedToReadGitObject = Raise<Error.FailedToReadGitObject>

private class GitObjectFile(val header: NonEmptyString, val content: ByteArray)

object GitObjectReader {
    context(RaiseFailedToReadGitObject, RaiseParsingFailed)
    fun read(gitRootDirectory: Path, gitObjectHash: GitObjectHash): GitObject {
        val gitObjectPath = gitRootDirectory
            .resolve("objects")
            .resolve(gitObjectHash.toString().substring(0, 2))
            .resolve(gitObjectHash.toString().substring(2))

        this@RaiseFailedToReadGitObject.ensure(Files.exists(gitObjectPath)) {
            Error.FailedToReadGitObject("Git object with hash '$gitObjectHash' does not exist on disk!")
        }

        return loadObject(gitObjectPath)
    }

    context(RaiseFailedToReadGitObject, RaiseParsingFailed)
    private fun loadObject(gitObjectPath: Path): GitObject {
        val file = try {
            readFile(gitObjectPath)
        } catch (exception: Exception) {
            raise(Error.FailedToReadGitObject(exception.localizedMessage))
        }

        this@RaiseFailedToReadGitObject.ensure(file.content.isNotEmpty()) {
            Error.FailedToReadGitObject("Git object file is empty")
        }

        val fileType = file.header.takeWhile { !it.isWhitespace() }.toString().lowercase()

        val parser = when (fileType) {
            "blob" -> GitBlobParser
            "commit" -> GitCommitParser
            "tree" -> GitTreeViewParser
            else -> raise(Error.UnknownGitObjectType(file.header))
        }

        return parser.parse(file.content)
    }

    context(RaiseFailedToReadGitObject)
    private fun readFile(gitObjectPath: Path): GitObjectFile =
        Files
            .newInputStream(gitObjectPath)
            .let { InflaterInputStream(it) }
            .use {
                val fullContent = it.readBytes()
                val indexOfFirstNull = fullContent.indexOfFirst { byte -> byte == 0.toByte() }

                val header = fullContent
                    .asList().subList(0, indexOfFirstNull)
                    .toStringUtf8()
                    .toNonEmptyStringOrNull()
                    ?: raise(Error.FailedToReadGitObject("File header is not expected to be empty"))

                val content = fullContent
                    .asList().subList(indexOfFirstNull + 1, fullContent.size)
                    .toByteArray()

                GitObjectFile(header, content)
            }
}

