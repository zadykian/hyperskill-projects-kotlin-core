package gitinternals.objects

import gitinternals.appendLine
import gitinternals.dateTimeFormatter

data class GitLogEntry(
    val commitHash: GitObjectHash,
    val commit: GitCommit,
    val wasMerged: Boolean
) {
    override fun toString() = buildString {
        appendLine("Commit: ", commitHash, if (wasMerged) " (merged)" else "")
        appendLine(commit.committer, " commit timestamp: ", dateTimeFormatter.format(commit.committedAt))
        append(commit.message.trim())
    }
}
