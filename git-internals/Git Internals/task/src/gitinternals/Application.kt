package gitinternals

import arrow.core.raise.either
import arrow.core.raise.ensure
import gitinternals.objects.*
import gitinternals.readers.GitBranchesReader
import gitinternals.readers.GitLogReader
import gitinternals.readers.GitObjectReader
import gitinternals.readers.GitTreeReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

data class IO(val read: () -> String, val write: (String) -> Unit)

class Application(private val io: IO) {
    fun run() {
        either { executeCommand() }.onLeft { io.write(it.displayText.toString()) }
    }

    context(RaiseAnyError)
    private fun executeCommand() {
        val gitRootDirectory = requestGitRootDirectory()
        val command = requestCommand()
        when (command) {
            Command.CatFile -> runCatFile(gitRootDirectory)
            Command.ListBranches -> runListBranches(gitRootDirectory)
            Command.Log -> runLog(gitRootDirectory)
            Command.CommitTree -> runCommitTree(gitRootDirectory)
        }
    }

    context(RaiseAnyError)
    private fun runCatFile(gitRootDirectory: Path) {
        val gitObjectHash = requestGitObjectHash(Requests.GIT_OBJECT_HASH)
        val gitObject = GitObjectReader.read(gitRootDirectory, gitObjectHash)

        val displayName = when (gitObject) {
            is GitCommit -> "commit"
            is GitTreeView, is GitTree -> "tree"
            is GitBlob -> "blob"
        }

        io.write("*${displayName.uppercase()}*")
        io.write(gitObject.toString())
    }

    context(RaiseFailedToReadGitBranch)
    private fun runListBranches(gitRootDirectory: Path) {
        val gitBranches = GitBranchesReader.readAll(gitRootDirectory)
        io.write(gitBranches.toString())
    }

    context(RaiseAnyError)
    private fun runLog(gitRootDirectory: Path) {
        io.write(Requests.GIT_BRANCH_NAME)
        val branchName = io.read().toNonEmptyStringOrNull() ?: raise(Error.InvalidInput("Branch name cannot be empty"))

        val branch = GitBranchesReader.read(gitRootDirectory, branchName)
        val gitLogEntries = GitLogReader.read(gitRootDirectory, branch)

        gitLogEntries.forEachIndexed { index, logEntry ->
            if (index > 0) io.write("")
            io.write(logEntry.toString())
        }
    }

    context(RaiseAnyError)
    private fun runCommitTree(gitRootDirectory: Path) {
        val commitHash = requestGitObjectHash(Requests.GIT_COMMIT_HASH)
        val gitCommit = GitObjectReader.read(gitRootDirectory, commitHash).ensureIs<GitCommit>()
        val gitTree = GitTreeReader.read(gitRootDirectory, gitCommit.tree)
        io.write(gitTree.toString())
    }

    context(RaiseInvalidInput)
    private fun requestGitObjectHash(request: String): GitObjectHash {
        io.write(request)
        val input = io.read()
        return GitObjectHash.fromStringOrNull(input) ?: raise(Error.InvalidInput("Invalid git object hash"))
    }

    context(RaiseInvalidInput)
    private fun requestCommand(): Command {
        io.write(Requests.COMMAND)
        val commandString = io.read()
        return Command.getByNameOrNull(commandString) ?: raise(Error.InvalidInput("Unknown command '$commandString'"))
    }

    context(RaiseInvalidInput)
    private fun requestGitRootDirectory(): Path {
        io.write(Requests.GIT_ROOT_DIRECTORY)
        val pathString = io.read()

        val path = try {
            Paths.get(pathString)
        } catch (exception: Exception) {
            raise(Error.InvalidInput("Specified path '$pathString' is invalid"))
        }

        ensure(Files.exists(path)) { Error.InvalidInput("Specified directory '$pathString' does not exist") }
        return path
    }

    private object Requests {
        const val COMMAND = "Enter command:"
        const val GIT_BRANCH_NAME = "Enter branch name:"
        const val GIT_COMMIT_HASH = "Enter commit-hash:"
        const val GIT_ROOT_DIRECTORY = "Enter .git directory location:"
        const val GIT_OBJECT_HASH = "Enter git object hash:"
    }
}
