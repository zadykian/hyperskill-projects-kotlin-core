package gitinternals

import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import gitinternals.objects.GitObjectHash
import gitinternals.readers.GitBranchesReader
import gitinternals.readers.GitLogReader
import gitinternals.readers.GitObjectReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

data class IO(val read: () -> String, val write: (String) -> Unit)

typealias RaiseDirectoryNotFound = Raise<Error.DirectoryNotFound>
typealias RaiseInvalidDirectoryPath = Raise<Error.InvalidDirectoryPath>
typealias RaiseInvalidGitObjectHash = Raise<Error.InvalidGitObjectHash>

class Application(private val io: IO) {
    fun run() {
        either { executeCommand() }.onLeft { io.write(it.displayText.toString()) }
    }

    context(Raise<Error>)
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

    context(Raise<Error>)
    private fun runCatFile(gitRootDirectory: Path) {
        io.write(Requests.GIT_OBJECT_HASH)
        val objectHashString = io.read()
        val gitObjectHash = GitObjectHash(objectHashString).bind()

        val gitObject = GitObjectReader.read(gitRootDirectory, gitObjectHash)
        io.write("*${gitObject::class.simpleName!!.replace("Git", "").uppercase()}*")
        io.write(gitObject.toString())
    }

    context(Raise<Error>)
    private fun runListBranches(gitRootDirectory: Path) {
        val gitBranches = GitBranchesReader.readAll(gitRootDirectory)
        io.write(gitBranches.toString())
    }

    context(Raise<Error>)
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

    context(Raise<Error>)
    private fun runCommitTree(gitRootDirectory: Path) {
        io.write(Requests.GIT_COMMIT_HASH)
        val input = io.read()
        val commitHash = GitObjectHash(input).bind()
    }

    context(Raise<Error.UnknownCommand>)
    private fun requestCommand(): Command {
        io.write(Requests.COMMAND)
        val input = io.read()
        return Command.byName(input).bind()
    }

    context(RaiseDirectoryNotFound, RaiseInvalidDirectoryPath)
    private fun requestGitRootDirectory(): Path {
        io.write(Requests.GIT_ROOT_DIRECTORY)
        val pathString = io.read()
        val path = try {
            Paths.get(pathString)
        } catch (exception: Exception) {
            raise(Error.InvalidDirectoryPath)
        }
        this@RaiseDirectoryNotFound.ensure(Files.exists(path)) { Error.DirectoryNotFound }
        return path
    }

    private object Requests {
        const val COMMAND = "Enter command:"
        const val GIT_BRANCH_NAME = "Enter branch name:"
        const val GIT_COMMIT_HASH = "Enter commit hash:"
        const val GIT_ROOT_DIRECTORY = "Enter .git directory location:"
        const val GIT_OBJECT_HASH = "Enter git object hash:"
    }
}
