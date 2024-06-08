package gitinternals.objects

import arrow.core.NonEmptyList
import gitinternals.NonEmptyString
import gitinternals.appendLine
import gitinternals.dateTimeFormatter
import java.time.ZonedDateTime

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

    data class UserData(val name: NonEmptyString, val email: NonEmptyString) {
        override fun toString() = "$name $email"
    }
}

sealed interface TreeNode : GitObject

data class GitBlob(val content: NonEmptyString) : TreeNode {
    override fun toString() = content.toString()
}

data class GitTree(val nodes: NonEmptyList<NamedNode>) : TreeNode {
    data class NamedNode(val name: NonEmptyString, val node: TreeNode)
}

data class GitTreeView(val nodes: NonEmptyList<NodeView>) : GitObject {
    override fun toString() = nodes.joinToString("\n")

    data class NodeView(val permissionMetadataNumber: UInt, val objectHash: GitObjectHash, val name: NonEmptyString) {
        override fun toString() = "$permissionMetadataNumber $objectHash $name"
    }
}
