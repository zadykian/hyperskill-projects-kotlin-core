package gitinternals.parse

import gitinternals.Error
import gitinternals.GitBlob
import gitinternals.toNonEmptyStringOrNull
import gitinternals.toStringUtf8

object GitBlobParser : GitObjectParser<GitBlob> {
    context(RaiseParsingFailed)
    override fun parse(content: ByteArray): GitBlob {
        val stringContent = content
            .toStringUtf8()
            .toNonEmptyStringOrNull() ?: raise(Error.ParsingFailed("Blob content cannot be empty"))

        return GitBlob(stringContent)
    }
}
