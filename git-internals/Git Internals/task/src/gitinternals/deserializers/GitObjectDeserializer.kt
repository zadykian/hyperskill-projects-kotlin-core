package gitinternals.deserializers

import arrow.core.raise.Raise
import gitinternals.Error
import gitinternals.objects.GitObject

typealias RaiseDeserializationFailed = Raise<Error.ParsingFailed>

interface GitObjectDeserializer<out TGitObject : GitObject> {
    context(RaiseDeserializationFailed)
    fun deserialize(content: ByteArray): TGitObject
}
