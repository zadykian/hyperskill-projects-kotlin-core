package gitinternals.readers

import gitinternals.Error
import gitinternals.NonEmptyString
import gitinternals.RaiseFailedToReadGitBranch
import gitinternals.objects.GitBranch
import gitinternals.objects.GitBranches
import gitinternals.objects.GitObjectHash
import gitinternals.toNonEmptyStringOrNull
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.asSequence

object GitBranchesReader {
    context(RaiseFailedToReadGitBranch)
    fun readAll(gitRootDirectory: Path): GitBranches {
        val allBranches = readAllBranchesFromDisk(gitRootDirectory)
        val currentBranchName = getCurrentBranchName(gitRootDirectory)
        val currentBranch = allBranches.find { it.name == currentBranchName }
            ?: raise(Error.FailedToReadGitBranch("Current branch '$currentBranchName' is missing"))
        val others = allBranches.minus(currentBranch)
        return GitBranches(currentBranch, others).bind()
    }

    context(RaiseFailedToReadGitBranch)
    fun read(gitRootDirectory: Path, branchName: NonEmptyString) =
        readAllBranchesFromDisk(gitRootDirectory).firstOrNull { it.name == branchName }
            ?: raise(Error.FailedToReadGitBranch("Git branch is not found"))

    context(RaiseFailedToReadGitBranch)
    private fun readAllBranchesFromDisk(gitRootDirectory: Path): List<GitBranch> {
        val branchesDir = gitRootDirectory.resolve("refs").resolve("heads")
        return try {
            Files.list(branchesDir).asSequence().map { readBranchFromDisk(it) }.toList()
        } catch (e: Exception) {
            raise(Error.FailedToReadGitBranch("Error occurred during branches enumeration: ${e.localizedMessage}"))
        }
    }

    context(RaiseFailedToReadGitBranch)
    private fun readBranchFromDisk(branchFile: Path): GitBranch {
        val fileName = branchFile.fileName.toString().toNonEmptyStringOrNull()
            ?: raise(Error.FailedToReadGitBranch("Branch file name '$branchFile' is invalid'"))

        val commitHashString = try {
            Files.readString(branchFile).trim()
        } catch (e: Exception) {
            raise(Error.FailedToReadGitBranch("Error occurred during branch file reading: '${e.localizedMessage}'"))
        }

        val commitHash = GitObjectHash.fromStringOrNull(commitHashString)
            ?: raise(Error.FailedToReadGitBranch("Branch file name '$branchFile' contains invalid commit hash"))
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
