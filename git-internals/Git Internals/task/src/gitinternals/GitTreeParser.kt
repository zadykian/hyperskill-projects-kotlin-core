package gitinternals

import arrow.core.raise.Raise

object GitTreeParser : GitObjectParser<GitObject.Tree> {
    context(Raise<Error.ParsingFailed>)
    override fun parse(lines: List<String>): GitObject.Tree {
        TODO()
    }
}
