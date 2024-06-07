package gitinternals.parse

import gitinternals.Blob
import gitinternals.Error
import gitinternals.toNonEmptyStringOrNull
import gitinternals.toStringUtf8

object GitBlobParser : GitObjectParser<Blob> {
    context(RaiseParsingFailed)
    override fun parse(content: ByteArray): Blob {
        val stringContent = content
            .toStringUtf8()
            .toNonEmptyStringOrNull() ?: raise(Error.ParsingFailed("Blob content cannot be empty"))

        return Blob(stringContent)
    }
}
