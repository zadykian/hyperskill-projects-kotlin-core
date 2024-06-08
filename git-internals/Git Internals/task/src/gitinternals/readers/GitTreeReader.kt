package gitinternals.readers

import gitinternals.Error
import gitinternals.RaiseDeserializationFailed
import gitinternals.RaiseFailedToReadGitObject
import gitinternals.objects.*
import java.nio.file.Path

object GitTreeReader {
    context(RaiseFailedToReadGitObject, RaiseDeserializationFailed)
    fun read(gitRootDirectory: Path, nodeHash: GitObjectHash): TreeNode =
        when (val gitObject = GitObjectReader.read(gitRootDirectory, nodeHash)) {
            is GitTreeView -> {
                val nodes = gitObject.nodes.map { GitTree.NamedNode(it.name, read(gitRootDirectory, it.objectHash)) }
                GitTree(nodeHash, nodes)
            }

            is GitBlob -> gitObject
            else -> raise(unexpectedType(gitObject))
        }

    private fun unexpectedType(gitObject: GitObject) =
        Error.FailedToReadGitObject(
            "Git object with hash '${gitObject.hash}' has unexpected type (${gitObject::class.simpleName})"
        )
}
