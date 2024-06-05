package gitinternals

import java.time.Instant

data class UserData(val name: String, val email: String)

sealed interface GitObject {
    data class Commit(
        val fileSystemTree: GitObjectHash,
        val parentCommitHashes: List<GitObjectHash>,
        val author: UserData,
        val createdAt: Instant,
        val committer: UserData,
        val appliedAt: Instant,
        val message: String,
    ) : GitObject

    data object Blob : GitObject
    data object Tree : GitObject
}
