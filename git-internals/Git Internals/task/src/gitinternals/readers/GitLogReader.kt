package gitinternals.readers

import gitinternals.Error
import gitinternals.RaiseDeserializationFailed
import gitinternals.RaiseFailedToReadGitObject
import gitinternals.objects.GitBranch
import gitinternals.objects.GitCommit
import gitinternals.objects.GitLogEntry
import gitinternals.objects.GitObjectHash
import java.nio.file.Path

object GitLogReader {
    context(RaiseFailedToReadGitObject, RaiseDeserializationFailed)
    fun read(gitRootDirectory: Path, branch: GitBranch) = sequence {
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
