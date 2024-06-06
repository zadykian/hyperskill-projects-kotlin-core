package gitinternals

import arrow.core.raise.Raise
import arrow.core.raise.ensure
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

typealias LineTokens = List<String>

object GitCommitParser : GitObjectParser<GitObject.Commit> {
    context(Raise<Error>)
    override fun parse(lines: List<String>): GitObject.Commit {
        val keyedLines = getKeyedLines(lines)
        fun get(key: String) = keyedLines[key] ?: raise(Error.ParsingFailed)

        val tree = GitObjectHash(get("tree").first().first()).bind()

        val parents = get("parent")
            .take(2)
            .map { GitObjectHash(it.first()) }
            .bindAll()

        val (author, createdAt) = userAndDate(get("author"))
        val (committer, committedAt) = userAndDate(get("committer"))

        // linesToSkip = treeLine + parentsLines + authorLine + commiterLine + blankLine
        val message = lines.drop(parents.size + 4).joinToString("\n")

        return GitObject.Commit(
            tree = tree,
            parents = parents,
            author = author,
            createdAt = createdAt,
            committer = committer,
            committedAt = committedAt,
            message = message
        )
    }

    context(Raise<Error.ParsingFailed>)
    private fun userAndDate(lineTokens: List<LineTokens>): Pair<UserData, ZonedDateTime> {
        ensure(lineTokens.isNotEmpty() && lineTokens.first().size == 4) { Error.ParsingFailed }
        val (name, email, timestamp, timezone) = lineTokens.first()
        return try {
            val user = UserData(name, email)
            val dateTime = Instant.parse(timestamp).atZone(ZoneOffset.of(timezone))
            Pair(user, dateTime)
        } catch (e: Exception) {
            raise(Error.ParsingFailed)
        }
    }

    context(Raise<Error.ParsingFailed>)
    private fun getKeyedLines(lines: List<String>): Map<String, List<LineTokens>> =
        lines
            .takeWhile { it.isNotBlank() }
            .mapIndexed { index, line -> Pair(index, line.split(' ').map(String::trim)) }
            .groupBy {
                ensure(it.second.size >= 2) { Error.ParsingFailed }
                it.second.first()
            }
            .mapValues {
                it.value.sortedBy { pair -> pair.first }.map { pair -> pair.second.drop(1) }
            }
}
