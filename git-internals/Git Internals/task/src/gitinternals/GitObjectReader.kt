package gitinternals

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.Raise
import arrow.core.raise.ensure
import arrow.core.right
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.InflaterInputStream

class GitObjectHash private constructor(val value: String) {
    override fun toString() = value

    companion object {
        private val sha1Regex = "[a-fA-F0-9]{40}".toRegex()
        private val sha256Regex = "[a-fA-F0-9]{64}".toRegex()

        operator fun invoke(gitObjectHash: String): Either<Error.InvalidGitObjectHash, GitObjectHash> =
            if (gitObjectHash.run { matches(sha1Regex) || matches(sha256Regex) })
                GitObjectHash(gitObjectHash).right()
            else Error.InvalidGitObjectHash.left()
    }
}

interface GitObjectParser<TGitObject : GitObject> {
    context(Raise<Error.ParsingFailed>) fun parse(lines: List<String>): TGitObject
}

object GitObjectReader {
    context(Raise<Error>)
    fun read(gitRootDirectory: Path, gitObjectHash: GitObjectHash): GitObject {
        val gitObjectPath = gitRootDirectory
            .resolve("objects")
            .resolve(gitObjectHash.value.substring(0, 2))
            .resolve(gitObjectHash.value.substring(2))

        ensure(Files.exists(gitObjectPath)) { Error.GitObjectNotFound }

        return loadObject(gitObjectPath)
    }

    context(Raise<Error>)
    private fun loadObject(gitObjectPath: Path): GitObject {
        val fileContentLines = try {
            readLines(gitObjectPath)
        } catch (exception: Exception) {
            raise(Error.FailedToReadGitObject(exception.localizedMessage))
        }

        ensure(fileContentLines.isNotEmpty()) { Error.FailedToReadGitObject() }
        val firstLine = fileContentLines.first()
        val firstLineTokens = firstLine.split(' ').map { it.trim() }

        val parser = when (firstLineTokens[0].lowercase()) {
            "blob" -> GitBlobParser
            "commit" -> GitCommitParser
            "tree" -> GitTreeParser
            else -> raise(Error.InvalidGitObjectHeader(firstLine))
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
