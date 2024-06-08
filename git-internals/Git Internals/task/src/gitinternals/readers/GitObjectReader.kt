package gitinternals.readers

import arrow.core.raise.ensure
import gitinternals.*
import gitinternals.deserializers.GitBlobDeserializer
import gitinternals.deserializers.GitCommitDeserializer
import gitinternals.deserializers.GitTreeViewDeserializer
import gitinternals.objects.GitObject
import gitinternals.objects.GitObjectHash
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.InflaterInputStream

object GitObjectReader {
    context(RaiseFailedToReadGitObject, RaiseDeserializationFailed)
    fun read(gitRootDirectory: Path, gitObjectHash: GitObjectHash): GitObject {
        val gitObjectPath = gitRootDirectory
            .resolve("objects")
            .resolve(gitObjectHash.toString().substring(0, 2))
            .resolve(gitObjectHash.toString().substring(2))

        this@RaiseFailedToReadGitObject.ensure(Files.exists(gitObjectPath)) {
            Error.FailedToReadGitObject("Git object with hash '$gitObjectHash' does not exist on disk!")
        }

        return loadObject(gitObjectHash, gitObjectPath)
    }

    context(RaiseFailedToReadGitObject, RaiseDeserializationFailed)
    private fun loadObject(gitObjectHash: GitObjectHash, gitObjectPath: Path): GitObject {
        val file = try {
            readFile(gitObjectPath)
        } catch (exception: Exception) {
            raise(Error.FailedToReadGitObject(exception.localizedMessage))
        }

        this@RaiseFailedToReadGitObject.ensure(file.content.isNotEmpty()) {
            Error.FailedToReadGitObject("Git object file is empty")
        }

        val fileType = file.header.takeWhile { !it.isWhitespace() }.toString().lowercase()

        val deserializer = when (fileType) {
            "blob" -> GitBlobDeserializer
            "commit" -> GitCommitDeserializer
            "tree" -> GitTreeViewDeserializer
            else -> raise(Error.FailedToReadGitObject("Unknown git object type '$fileType'"))
        }

        return deserializer.deserialize(gitObjectHash, file.content)
    }

    context(RaiseFailedToReadGitObject)
    private fun readFile(gitObjectPath: Path): GitObjectFile =
        Files
            .newInputStream(gitObjectPath)
            .let { InflaterInputStream(it) }
            .use {
                val fullContent = it.readBytes()
                val indexOfFirstNull = fullContent.indexOfFirst { byte -> byte == 0.toByte() }

                val header = fullContent
                    .asList().subList(0, indexOfFirstNull)
                    .toStringUtf8()
                    .toNonEmptyStringOrNull()
                    ?: raise(Error.FailedToReadGitObject("File header is not expected to be empty"))

                val content = fullContent
                    .asList().subList(indexOfFirstNull + 1, fullContent.size)
                    .toByteArray()

                GitObjectFile(header, content)
            }

    private class GitObjectFile(val header: NonEmptyString, val content: ByteArray)
}
