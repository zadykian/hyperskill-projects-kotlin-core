package gitinternals

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.Raise
import arrow.core.right
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.asSequence

typealias RaiseFailedToReadGitBranch = Raise<Error.FailedToReadGitBranch>

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

object GitBranchesReader {
    context(RaiseFailedToReadGitBranch, RaiseInvalidGitObjectHash)
    fun readAll(gitRootDirectory: Path): GitBranches {
        val allBranches = readAllBranchesFromDisk(gitRootDirectory)
        val currentBranchName = getCurrentBranchName(gitRootDirectory)
        val currentBranch = allBranches.find { it.name == currentBranchName }
            ?: raise(Error.FailedToReadGitBranch("Current branch '$currentBranchName' is missing"))
        val others = allBranches.minus(currentBranch)
        return GitBranches(currentBranch, others).bind()
    }

    context(RaiseFailedToReadGitBranch, RaiseInvalidGitObjectHash)
    fun read(gitRootDirectory: Path, branchName: NonEmptyString) =
        readAllBranchesFromDisk(gitRootDirectory).firstOrNull { it.name == branchName }
            ?: raise(Error.GitBranchNotFound)

    context(RaiseFailedToReadGitBranch, RaiseInvalidGitObjectHash)
    private fun readAllBranchesFromDisk(gitRootDirectory: Path): List<GitBranch> {
        val branchesDir = gitRootDirectory.resolve("refs").resolve("heads")
        return try {
            Files.list(branchesDir).asSequence().map { readBranchFromDisk(it) }.toList()
        } catch (e: Exception) {
            raise(Error.FailedToReadGitBranch("Error occurred during branches enumeration: ${e.localizedMessage}"))
        }
    }

    context(RaiseFailedToReadGitBranch, RaiseInvalidGitObjectHash)
    private fun readBranchFromDisk(branchFile: Path): GitBranch {
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
