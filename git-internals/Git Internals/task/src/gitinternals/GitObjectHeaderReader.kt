package gitinternals

import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.InflaterInputStream

enum class GitObjectType {
    Blob,
    Commit,
    Tree,
}

class GitObjectHeader(private val type: GitObjectType, private val sizeInBytes: Long) {
    override fun toString() = "type:{${type.toString().lowercase()}} length:$sizeInBytes"
}

object GitObjectHeaderReader {
    private val sha1Regex = "[a-fA-F0-9]{40}".toRegex()
    private val sha256Regex = "[a-fA-F0-9]{64}".toRegex()

    fun read(gitRootDirectory: Path, gitObjectHash: String): Result<GitObjectHeader> {
        if (Files.notExists(gitRootDirectory)) {
            return Failure(Errors.DIRECTORY_NOT_FOUND)
        }

        if (!gitObjectHash.run { matches(sha1Regex) || matches(sha256Regex) }) {
            return Failure(Errors.INVALID_GIT_OBJECT_HASH)
        }

        val gitObjectPath = gitRootDirectory
            .resolve(gitObjectHash.substring(0, 2))
            .resolve(gitObjectHash.substring(2))

        if (Files.notExists(gitObjectPath)) {
            return Failure(Errors.GIT_OBJECT_NOT_FOUND)
        }

        val decompressedContent = Files
            .newInputStream(gitObjectPath)
            .let { InflaterInputStream(it) }
            .use { stream ->
                stream.reader().use { it.read() }
            }

        val splitByNullChar = decompressedContent
            .flatMap { it.split('\u0000') }
            .joinToString("\n")
    }
}