package gitinternals

import arrow.core.raise.Raise

object GitBlobParser : GitObjectParser<GitObject.Blob> {
    context(Raise<Error.ParsingFailed>)
    override fun parse(lines: List<String>): GitObject.Blob =
        GitObject.Blob(lines.joinToString("\n"))
}
