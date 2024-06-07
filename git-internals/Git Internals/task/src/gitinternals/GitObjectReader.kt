package gitinternals

import arrow.core.raise.Raise
import arrow.core.raise.ensure
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.InflaterInputStream

typealias RaiseFailedToReadGitObject = Raise<Error.FailedToReadGitObject>
typealias RaiseParsingFailed = Raise<Error.ParsingFailed>

interface GitObjectParser<out TGitObject : GitObject> {
    context(RaiseParsingFailed) fun parse(lines: List<String>): TGitObject
}

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
        val fileContentLines = try {
            readLines(gitObjectPath)
        } catch (exception: Exception) {
            raise(Error.FailedToReadGitObject(exception.localizedMessage))
        }

        this@RaiseFailedToReadGitObject.ensure(fileContentLines.isNotEmpty()) {
            Error.FailedToReadGitObject("Git object file is empty")
        }

        val firstLine = fileContentLines.first()
        val firstLineTokens = firstLine.split(' ').map { it.trim() }

        val parser = when (firstLineTokens[0].lowercase()) {
            "blob" -> GitBlobParser
            "commit" -> GitCommitParser
            "tree" -> GitTreeParser
            else -> raise(Error.UnknownGitObjectType(firstLine))
        }

        return parser.parse(fileContentLines.drop(1))
    }

    private fun readLines(gitObjectPath: Path): List<String> =
        Files
            .newInputStream(gitObjectPath)
            .let { InflaterInputStream(it) }
            .bufferedReader()
            .use {
                buildList {
                    val headerLine = it.readWhile { char -> char != '\u0000' }.joinToString(separator = "")
                    add(headerLine)
                    it.forEachLine { add(it) }
                }
            }
}
