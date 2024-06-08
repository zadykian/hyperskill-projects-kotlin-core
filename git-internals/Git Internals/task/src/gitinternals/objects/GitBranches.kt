package gitinternals.objects

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import gitinternals.Error
import gitinternals.NonEmptyString

data class GitBranch(val name: NonEmptyString, val commitHash: GitObjectHash)

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
            return if (current in othersSet)
                Error.InvalidGitBranches("Current branch should not belong to the set of others").left()
            else GitBranches(current, othersSet).right()
        }
    }
}
