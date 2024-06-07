package gitinternals.parse

import arrow.core.NonEmptyList
import gitinternals.Tree

object GitTreeParser : GitObjectParser<Tree> {
    context(RaiseParsingFailed)
    override fun parse(lines: NonEmptyList<String>): Tree {
        TODO()
    }
}
