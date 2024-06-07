package gitinternals

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.left
import arrow.core.right
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class UserData(val name: String, val email: String) {
    override fun toString() = "$name $email"
}

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

sealed interface GitObject

data class Commit(
    val tree: GitObjectHash,
    val parents: List<GitObjectHash>,
    val author: UserData,
    val createdAt: ZonedDateTime,
    val committer: UserData,
    val committedAt: ZonedDateTime,
    val message: String,
) : GitObject {
    override fun toString() = buildString {
        fun <T> appendLine(vararg values: T) = append(*values).append("\n")
        appendLine("tree: ", tree)
        if (parents.isNotEmpty()) {
            appendLine("parents: ", parents.joinToString(separator = " | "))
        }
        appendLine("author: ", author, " original timestamp: ", dateTimeFormatter.format(createdAt))
        appendLine("committer: ", committer, " commit timestamp: ", dateTimeFormatter.format(committedAt))
        append("commit message:", message)
    }

    companion object {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxx")
    }
}

data class Blob(val content: String) : GitObject {
    override fun toString() = content
}

data class Tree(val nodes: NonEmptyList<Node>) : GitObject {
    override fun toString() = nodes.joinToString("\n")

    data class Node(val permissionMetadataNumber: UInt, val fileHash: GitObjectHash, val fileName: String) {
        override fun toString() = "$permissionMetadataNumber $fileHash $fileName"
    }
}
