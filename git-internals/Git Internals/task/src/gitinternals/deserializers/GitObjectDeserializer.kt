package gitinternals.deserializers

import gitinternals.RaiseDeserializationFailed
import gitinternals.objects.GitObject

interface GitObjectDeserializer<out TGitObject : GitObject> {
    context(RaiseDeserializationFailed)
    fun deserialize(content: ByteArray): TGitObject
}
