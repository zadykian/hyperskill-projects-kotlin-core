package gitinternals

import java.nio.file.Path

data class GitLogEntry(
    val commitHash: GitObjectHash,
    val commit: GitCommit,
    val wasMerged: Boolean
) {
    override fun toString() = buildString {
        appendLine("Commit: ", commitHash, if (wasMerged) " (merged)" else "")
        appendLine(commit.committer, " commit timestamp: ", dateTimeFormatter.format(commit.committedAt))
        appendLine(commit.message)
    }
}

object GitLogReader {
    fun read(gitRootDirectory: Path, branch: GitBranch): Sequence<GitLogEntry> {
        TODO()
    }
}