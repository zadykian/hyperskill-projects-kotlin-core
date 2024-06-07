package gitinternals

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import java.nio.file.Path

data class GitBranch(val name: NonEmptyString)

class GitBranches private constructor(private val current: GitBranch, private val others: Set<GitBranch>) {
    override fun toString() =
        others
            .asSequence()
            .plus(current)
            .sortedBy { it.name }
            .joinToString("\n") { "${(if (it === current) "* " else "  ")}${it.name}" }

    companion object {
        operator fun invoke(
            current: GitBranch,
            others: List<GitBranch>
        ): Either<Error.InvalidGitBranches, GitBranches> {
            val othersSet = others.toSet()
            return if (current in othersSet) Error.InvalidGitBranches.left()
            else GitBranches(current, othersSet).right()
        }
    }
}

object GitBranchesReader {
    fun read(gitRootDirectory: Path): GitBranches {
        TODO()
    }
}
