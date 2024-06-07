package gitinternals.parse

import gitinternals.NonEmptyString
import gitinternals.Tree

object GitTreeParser : GitObjectParser<Tree> {
    context(RaiseParsingFailed)
    override fun parse(content: NonEmptyString): Tree {
        TODO()
    }
}
