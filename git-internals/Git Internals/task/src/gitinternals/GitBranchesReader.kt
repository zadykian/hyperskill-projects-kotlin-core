package gitinternals

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.Raise
import arrow.core.right
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.asSequence

typealias RaiseFailedToReadGitBranch = Raise<Error.FailedToReadGitBranch>
typealias RaiseInvalidGitBranches = Raise<Error.InvalidGitBranches>

data class GitBranch(val name: NonEmptyString, val commandHash: GitObjectHash)

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

object GitBranchesReader {
    context(RaiseFailedToReadGitBranch, RaiseInvalidGitBranches, RaiseInvalidGitObjectHash)
    fun readAll(gitRootDirectory: Path): GitBranches {
        fun raise(text: CharSequence): Nothing = raise(Error.FailedToReadGitBranch(text))
        val branchesDir = gitRootDirectory.resolve("refs").resolve("heads")

        val allBranches = try {
            Files.list(branchesDir).asSequence().map { read(it) }.toList()
        } catch (e: Exception) {
            raise("Error occurred during branches enumeration: ${e.localizedMessage}")
        }

        val currentBranchName = getCurrentBranchName(gitRootDirectory)
        val currentBranch = allBranches.find { it.name == currentBranchName }
            ?: raise("Current branch '$currentBranchName' does not present in the list of all branches")
        val others = allBranches.minus(currentBranch)
        return GitBranches(currentBranch, others).bind()
    }

    context(RaiseFailedToReadGitBranch, RaiseInvalidGitBranches, RaiseInvalidGitObjectHash)
    fun read(branchFile: Path): GitBranch {
        val fileName = branchFile.fileName.toString().toNonEmptyStringOrNull()
            ?: raise(Error.InvalidGitBranches("Branch file name '$branchFile' is invalid'"))

        val commitHashString = try {
            Files.readString(branchFile).trim()
        } catch (e: Exception) {
            raise(Error.FailedToReadGitBranch("Error occurred during branch file reading: '${e.localizedMessage}'"))
        }

        val commitHash = GitObjectHash(commitHashString).bind()
        return GitBranch(fileName, commitHash)
    }

    context(RaiseFailedToReadGitBranch)
    private fun getCurrentBranchName(gitRootDirectory: Path): NonEmptyString {
        val headFilePath = gitRootDirectory.resolve("HEAD")
        val fileContent = try {
            Files.readString(headFilePath)
        } catch (e: Exception) {
            raise(Error.FailedToReadGitBranch("Failed to read HEAD file's content: ${e.localizedMessage}"))
        }

        return fileContent.replaceFirst("ref: refs/heads/", "").trim().toNonEmptyStringOrNull()
            ?: raise(Error.FailedToReadGitBranch("Unexpected HEAD file content: '$fileContent'"))
    }
}
