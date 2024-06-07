package gitinternals

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.left
import arrow.core.right
import java.time.ZonedDateTime

data class UserData(val name: NonEmptyString, val email: NonEmptyString) {
    override fun toString() = "$name $email"
}

class GitObjectHash private constructor(private val hexValue: String) {
    override fun toString() = hexValue

    companion object {
        private val sha1Regex = "[a-fA-F0-9]{40}".toRegex()
        private val sha256Regex = "[a-fA-F0-9]{64}".toRegex()

        operator fun invoke(gitObjectHash: String): Either<Error.InvalidGitObjectHash, GitObjectHash> =
            if (gitObjectHash.run { matches(sha1Regex) || matches(sha256Regex) })
                GitObjectHash(gitObjectHash.lowercase()).right()
            else Error.InvalidGitObjectHash.left()

        operator fun invoke(bytes: List<Byte>): Either<Error.InvalidGitObjectHash, GitObjectHash> {
            if (bytes.size != 20 && bytes.size != 32) {
                return Error.InvalidGitObjectHash.left()
            }

            val hexString = bytes.joinToString(separator = "") { String.format("%02x", it) }
            return GitObjectHash(hexString).right()
        }
    }
}

sealed interface GitObject

data class GitCommit(
    val tree: GitObjectHash,
    val parents: List<GitObjectHash>,
    val author: UserData,
    val createdAt: ZonedDateTime,
    val committer: UserData,
    val committedAt: ZonedDateTime,
    val message: NonEmptyString,
) : GitObject {
    override fun toString() = buildString {
        appendLine("tree: ", tree)
        if (parents.isNotEmpty()) {
            appendLine("parents: ", parents.joinToString(separator = " | "))
        }
        appendLine("author: ", author, " original timestamp: ", dateTimeFormatter.format(createdAt))
        appendLine("committer: ", committer, " commit timestamp: ", dateTimeFormatter.format(committedAt))
        append("commit message:", message)
    }
}

data class GitBlob(val content: NonEmptyString) : GitObject {
    override fun toString() = content.toString()
}

data class GitTree(val nodes: NonEmptyList<Node>) : GitObject {
    override fun toString() = nodes.joinToString("\n")

    data class Node(val permissionMetadataNumber: UInt, val fileHash: GitObjectHash, val fileName: NonEmptyString) {
        override fun toString() = "$permissionMetadataNumber $fileHash $fileName"
    }
}
