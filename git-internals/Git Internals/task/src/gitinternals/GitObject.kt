package gitinternals

import arrow.core.NonEmptyList
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class UserData(val name: String, val email: String) {
    override fun toString() = "$name $email"
}

sealed interface GitObject {
    data class Commit(
        val tree: GitObjectHash,
        val parents: NonEmptyList<GitObjectHash>,
        val author: UserData,
        val createdAt: ZonedDateTime,
        val committer: UserData,
        val committedAt: ZonedDateTime,
        val message: String,
    ) : GitObject {
        override fun toString() = buildString {
            fun <T> appendLine(vararg values: T) = append(values).append("\n")
            appendLine("tree: ", tree)
            appendLine("parents: ", parents.joinToString(separator = "|"))
            appendLine("author: ", author, "original timestamp: ", dateTimeFormatter.format(createdAt))
            appendLine("committer: ", committer, "commit timestamp: ", dateTimeFormatter.format(committedAt))
            append("commit message: ", message)
        }

        companion object {
            private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss x")
        }
    }

    data object Blob : GitObject
    data object Tree : GitObject
}
