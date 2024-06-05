package gitinternals

import arrow.core.raise.Raise
import arrow.core.raise.ensure
import arrow.core.toNonEmptyListOrNull
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

object GitCommitParser : GitObjectParser<GitObject.Commit> {
    context(Raise<Error>)
    override fun parse(lines: List<String>): GitObject.Commit {
        val keyedLines = getKeyedValues(lines)
        fun get(key: String) = keyedLines[key] ?: raise(Error.ParsingFailed)

        val tree = GitObjectHash(get("tree").first().first()).bind()

        val parents = get("parent")
            .map { GitObjectHash(it.first()) }
            .bindAll()
            .toNonEmptyListOrNull() ?: raise(Error.ParsingFailed)

        fun userAndDate(key: String): Pair<UserData, ZonedDateTime> {
            val tokens = get(key)
            ensure(tokens.size == 1 && tokens.first().size == 4) { Error.ParsingFailed }
            val (name, email, timestamp, timezone) = tokens.first()
            try {
                val user = UserData(name, email)
                val dateTime = Instant.parse(timestamp).atZone(ZoneOffset.of(timezone))
                return Pair(user, dateTime)
            } catch (e: Exception) {
                raise(Error.ParsingFailed)
            }
        }

        val (author, createdAt) = userAndDate("author")
        val (committer, committedAt) = userAndDate("committer")

        val message = lines.dropWhile { }

        return GitObject.Commit(
            tree = tree,
            parents = parents,
            author = author,
            createdAt = createdAt,
            committer = committer,
            committedAt = committedAt,
            message = ""
        )
    }

    context(Raise<Error.ParsingFailed>)
    private fun getKeyedValues(lines: List<String>): Map<String, List<List<String>>> {
        val keyedLines = lines
            .mapIndexed { index, line -> Pair(index, line.split(' ').map(String::trim)) }
            .groupBy {
                ensure(it.second.size >= 2) { Error.ParsingFailed }
                it.second.first()
            }
            .mapValues {
                it.value.sortedBy { idx -> idx.first }.map { pair -> pair.second.drop(1) }
            }
        return keyedLines
    }
}
