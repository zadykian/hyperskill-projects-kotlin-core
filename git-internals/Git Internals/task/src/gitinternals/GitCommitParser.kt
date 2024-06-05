package gitinternals

import arrow.core.raise.Raise

object GitCommitParser : GitObjectParser<GitObject.Commit> {
    context(Raise<Error.ParsingFailed>)
    override fun parse(lines: List<String>): GitObject.Commit {
        TODO()
    }
}
