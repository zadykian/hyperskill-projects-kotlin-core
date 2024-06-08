package gitinternals.deserializers

import gitinternals.Error
import gitinternals.RaiseDeserializationFailed
import gitinternals.objects.GitBlob
import gitinternals.toNonEmptyStringOrNull
import gitinternals.toStringUtf8

object GitBlobDeserializer : GitObjectDeserializer<GitBlob> {
    context(RaiseDeserializationFailed)
    override fun deserialize(content: ByteArray): GitBlob {
        val stringContent = content
            .toStringUtf8()
            .toNonEmptyStringOrNull() ?: raise(Error.DeserializationFailed("Blob content cannot be empty"))

        return GitBlob(stringContent)
    }
}
