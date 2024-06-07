package gitinternals.parse

import arrow.core.NonEmptyList
import gitinternals.Blob

object GitBlobParser : GitObjectParser<Blob> {
    context(RaiseParsingFailed)
    override fun parse(lines: NonEmptyList<String>): Blob = Blob(lines.joinToString("\n"))
}
