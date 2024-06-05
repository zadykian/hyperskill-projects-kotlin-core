package gitinternals

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.Raise
import arrow.core.raise.ensure
import arrow.core.right
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.InflaterInputStream

enum class GitObjectType {
    Blob,
    Commit,
    Tree,
}

class GitObjectHeader(private val type: GitObjectType, private val sizeInBytes: ULong) {
    override fun toString() = "type:${type.toString().lowercase()} length:$sizeInBytes"
}

class GitObjectHash private constructor(val value: String) {
    companion object {
        private val sha1Regex = "[a-fA-F0-9]{40}".toRegex()
        private val sha256Regex = "[a-fA-F0-9]{64}".toRegex()

        operator fun invoke(gitObjectHash: String): Either<Error.InvalidGitObjectHash, GitObjectHash> =
            if (gitObjectHash.run { matches(sha1Regex) || matches(sha256Regex) })
                GitObjectHash(gitObjectHash).right()
            else Error.InvalidGitObjectHash.left()
    }
}

private typealias RaiseGitObjectNotFound = Raise<Error.GitObjectNotFound>
private typealias RaiseInvalidGitObjectHeader = Raise<Error.InvalidGitObjectHeader>
private typealias RaiseFailedToReadGitObject = Raise<Error.FailedToReadGitObject>

object GitObjectHeaderReader {
    context(RaiseGitObjectNotFound, RaiseInvalidGitObjectHeader, RaiseFailedToReadGitObject)
    fun read(gitRootDirectory: Path, gitObjectHash: GitObjectHash): GitObjectHeader {
        val gitObjectPath = gitRootDirectory
            .resolve("objects")
            .resolve(gitObjectHash.value.substring(0, 2))
            .resolve(gitObjectHash.value.substring(2))

        this@RaiseGitObjectNotFound.ensure(Files.exists(gitObjectPath)) { Error.GitObjectNotFound }

        return loadObjectHeader(gitObjectPath)
    }

    context(RaiseInvalidGitObjectHeader, RaiseFailedToReadGitObject)
    private fun loadObjectHeader(gitObjectPath: Path): GitObjectHeader {
        val firstLine = try {
            readFirstLine(gitObjectPath)
        } catch (exception: Exception) {
            raise(Error.FailedToReadGitObject(exception.localizedMessage))
        }

        val firstLineTokens = firstLine.split(' ').map { it.trim() }

        val objectType = when (firstLineTokens[0].lowercase()) {
            "blob" -> GitObjectType.Blob
            "commit" -> GitObjectType.Commit
            "tree" -> GitObjectType.Tree
            else -> raise(Error.InvalidGitObjectHeader(firstLine))
        }

        val sizeInBytes = firstLineTokens[1].toULongOrNull()
            ?: raise(Error.InvalidGitObjectHeader(firstLine))

        return GitObjectHeader(objectType, sizeInBytes)
    }

    private fun readFirstLine(gitObjectPath: Path) =
        Files
            .newInputStream(gitObjectPath)
            .let { InflaterInputStream(it) }
            .bufferedReader()
            .use { it.readWhile { char -> char != '\u0000' }.joinToString(separator = "") }
}
