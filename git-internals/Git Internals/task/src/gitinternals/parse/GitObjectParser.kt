package gitinternals.parse

import arrow.core.raise.Raise
import gitinternals.Error
import gitinternals.GitObject
import gitinternals.NonEmptyString

typealias RaiseParsingFailed = Raise<Error.ParsingFailed>

interface GitObjectParser<out TGitObject : GitObject> {
    context(RaiseParsingFailed) fun parse(content: NonEmptyString): TGitObject
}
