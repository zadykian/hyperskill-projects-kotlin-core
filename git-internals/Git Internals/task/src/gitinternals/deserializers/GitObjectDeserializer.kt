package gitinternals.deserializers

import gitinternals.RaiseDeserializationFailed
import gitinternals.objects.GitObject
import gitinternals.objects.GitObjectHash

interface GitObjectDeserializer<out TGitObject : GitObject> {
    context(RaiseDeserializationFailed)
    fun deserialize(objectHash: GitObjectHash, content: ByteArray): TGitObject
}
