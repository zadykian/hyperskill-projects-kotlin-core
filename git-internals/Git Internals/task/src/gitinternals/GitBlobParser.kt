package gitinternals

object GitBlobParser : GitObjectParser<GitObject.Blob> {
    context(RaiseParsingFailed)
    override fun parse(lines: List<String>): GitObject.Blob =
        GitObject.Blob(lines.joinToString("\n"))
}
