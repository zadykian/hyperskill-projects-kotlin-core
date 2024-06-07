package gitinternals.parse

import arrow.core.raise.Raise
import gitinternals.Error
import gitinternals.GitObject

typealias RaiseParsingFailed = Raise<Error.ParsingFailed>

interface GitObjectParser<out TGitObject : GitObject> {
    context(RaiseParsingFailed)
    fun parse(content: ByteArray): TGitObject
}
