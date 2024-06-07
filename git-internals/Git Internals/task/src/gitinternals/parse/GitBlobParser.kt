package gitinternals.parse

import gitinternals.Blob
import gitinternals.NonEmptyString

object GitBlobParser : GitObjectParser<Blob> {
    context(RaiseParsingFailed)
    override fun parse(content: NonEmptyString): Blob = Blob(content)
}
