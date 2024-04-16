package gitinternals

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

object GitObjectHeaderReader {
    private val sha1Regex = "[a-fA-F0-9]{40}".toRegex()
    private val sha256Regex = "[a-fA-F0-9]{64}".toRegex()

    fun read(gitRootDirectory: Path, gitObjectHash: String): Result<GitObjectHeader> {
        if (!gitObjectHash.run { matches(sha1Regex) || matches(sha256Regex) }) {
            return Failure(Errors.INVALID_GIT_OBJECT_HASH)
        }

        val gitObjectPath = gitRootDirectory
            .resolve("objects")
            .resolve(gitObjectHash.substring(0, 2))
            .resolve(gitObjectHash.substring(2))

        if (Files.notExists(gitObjectPath)) {
            return Failure(Errors.GIT_OBJECT_NOT_FOUND)
        }

        return loadObjectHeader(gitObjectPath)
    }

    private fun loadObjectHeader(gitObjectPath: Path): Result<GitObjectHeader> {
        val firstLine = Files
            .newInputStream(gitObjectPath)
            .let { InflaterInputStream(it) }
            .bufferedReader()
            .use { it.readWhile { char -> char != '\u0000' }.joinToString(separator = "") }

        val firstLineTokens = firstLine.split(' ').map { it.trim() }

        val objectType = when (firstLineTokens[0].lowercase()) {
            "blob" -> GitObjectType.Blob
            "commit" -> GitObjectType.Commit
            "tree" -> GitObjectType.Tree
            else -> return Failure(Errors.invalidGitObjectHeader(firstLine))
        }

        val sizeInBytes = firstLineTokens[1].toULongOrNull()
            ?: return Failure(Errors.invalidGitObjectHeader(firstLine))

        return Success(GitObjectHeader(objectType, sizeInBytes))
    }
}
