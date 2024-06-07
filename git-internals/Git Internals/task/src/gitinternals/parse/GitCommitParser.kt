package gitinternals.parse

import arrow.core.raise.ensure
import gitinternals.*
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

private typealias LineTokens = List<String>

object GitCommitParser : GitObjectParser<Commit> {
    context(RaiseParsingFailed)
    override fun parse(content: NonEmptyString): Commit {
        val contentLines = content.split("\n")
        val keyedLines = getKeyedLines(contentLines)
        fun get(key: String) = keyedLines[key] ?: emptyList()

        val tree = GitObjectHash(get("tree").first().first()).bind()
        val parents = get("parent").take(2).map { GitObjectHash(it.first()) }.bindAll()
        val (author, createdAt) = userAndDate(get("author"))
        val (committer, committedAt) = userAndDate(get("committer"))

        val message = contentLines.drop(parents.size + 3).joinToString("\n").toNonEmptyStringOrNull()
            ?: raise(Error.ParsingFailed("Commit message cannot be empty"))

        return Commit(
            tree = tree,
            parents = parents,
            author = author,
            createdAt = createdAt,
            committer = committer,
            committedAt = committedAt,
            message = message
        )
    }

    context(RaiseParsingFailed)
    private fun userAndDate(lineTokens: List<LineTokens>): Pair<UserData, ZonedDateTime> {
        ensure(lineTokens.isNotEmpty() && lineTokens.first().size == 4) {
            Error.ParsingFailed("Unexpected line tokens: [${lineTokens.joinToString()}]")
        }
        val (name, email, timestamp, timezone) = lineTokens.first()

        val nameValue = name.toNonEmptyStringOrNull()
            ?: raise(Error.ParsingFailed("User's name cannot be empty"))
        val emailValue = email.trim('<', '>').toNonEmptyStringOrNull()
            ?: raise(Error.ParsingFailed("Email cannot be empty"))

        return try {
            val user = UserData(nameValue, emailValue)
            val unixEpoch = timestamp.toLongOrNull() ?: raise(Error.ParsingFailed("Invalid timestamp '$timestamp'"))
            val dateTime = Instant.ofEpochSecond(unixEpoch).atZone(ZoneOffset.of(timezone))
            Pair(user, dateTime)
        } catch (e: Exception) {
            raise(Error.ParsingFailed("Failed to parse UserData and ZonedDateTime (${e.localizedMessage})"))
        }
    }

    context(RaiseParsingFailed)
    private fun getKeyedLines(lines: List<String>): Map<String, List<LineTokens>> =
        lines
            .takeWhile { it.isNotBlank() }
            .mapIndexed { index, line -> Pair(index, line.split(' ').map(String::trim)) }
            .groupBy {
                ensure(it.second.size >= 2) {
                    Error.ParsingFailed("Line ${it.first} has unexpected content: ${lines[it.first]}")
                }
                it.second.first()
            }
            .mapValues {
                it.value.sortedBy { pair -> pair.first }.map { pair -> pair.second.drop(1) }
            }
}
