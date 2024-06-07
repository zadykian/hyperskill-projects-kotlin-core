package gitinternals

object GitTreeParser : GitObjectParser<GitObject.Tree> {
    context(RaiseParsingFailed)
    override fun parse(lines: List<String>): GitObject.Tree {
        TODO()
    }
}
