package gitinternals

import arrow.core.raise.Raise
import java.nio.file.Path

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

object GitLogReader {
    context(Raise<Error>)
    fun read(gitRootDirectory: Path, branch: GitBranch) = sequence<GitLogEntry> {
        fun getCommit(commitHash: GitObjectHash): GitCommit {
            val gitObject = GitObjectReader.read(gitRootDirectory, commitHash)
            return if (gitObject is GitCommit) gitObject
            else raise(Error.FailedToReadGitObject("Object with hash '$commitHash' is not a commit"))
        }

        var nextCommitHash: GitObjectHash? = branch.commitHash
        while (nextCommitHash != null) {
            val commit = getCommit(nextCommitHash)
            val entry = GitLogEntry(nextCommitHash, commit, wasMerged = false)
            yield(entry)

            val mergedParentEntries = commit
                .parents
                .asSequence()
                .drop(1)
                .map { hash -> GitLogEntry(hash, getCommit(hash), wasMerged = true) }

            yieldAll(mergedParentEntries)
            nextCommitHash = commit.parents.firstOrNull()
        }
    }
}
