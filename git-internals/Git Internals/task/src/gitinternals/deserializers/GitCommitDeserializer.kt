package gitinternals.deserializers

import arrow.core.raise.ensure
import gitinternals.Error
import gitinternals.RaiseDeserializationFailed
import gitinternals.objects.GitCommit
import gitinternals.objects.GitObjectHash
import gitinternals.toNonEmptyStringOrNull
import gitinternals.toStringUtf8
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

private typealias LineTokens = List<String>

object GitCommitDeserializer : GitObjectDeserializer<GitCommit> {
    context(RaiseDeserializationFailed)
    override fun deserialize(objectHash: GitObjectHash, content: ByteArray): GitCommit {
        val contentLines = content.toStringUtf8().split("\n")
        val keyedLines = getKeyedLines(contentLines)
        fun get(key: String) = keyedLines[key] ?: emptyList()

        val tree = GitObjectHash.fromStringOrNull(get("tree").first().first())
            ?: raise(Error.DeserializationFailed("Commit contains invalid tree hash"))

        val parents = get("parent")
            .map {
                GitObjectHash.fromStringOrNull(it.first())
                    ?: raise(Error.DeserializationFailed("Commit contains invalid parent hash"))
            }

        val (author, createdAt) = userAndDate(get("author"))
        val (committer, committedAt) = userAndDate(get("committer"))

        val message = contentLines.drop(parents.size + 3).joinToString("\n").toNonEmptyStringOrNull()
            ?: raise(Error.DeserializationFailed("Commit message cannot be empty"))

        return GitCommit(
            hash = objectHash,
            tree = tree,
            parents = parents,
            author = author,
            createdAt = createdAt,
            committer = committer,
            committedAt = committedAt,
            message = message
        )
    }

    context(RaiseDeserializationFailed)
    private fun userAndDate(lineTokens: List<LineTokens>): Pair<GitCommit.UserData, ZonedDateTime> {
        ensure(lineTokens.isNotEmpty() && lineTokens.first().size == 4) {
            Error.DeserializationFailed("Unexpected line tokens: [${lineTokens.joinToString()}]")
        }
        val (name, email, timestamp, timezone) = lineTokens.first()

        val nameValue = name.toNonEmptyStringOrNull()
            ?: raise(Error.DeserializationFailed("User's name cannot be empty"))
        val emailValue = email.trim('<', '>').toNonEmptyStringOrNull()
            ?: raise(Error.DeserializationFailed("Email cannot be empty"))

        return try {
            val user = GitCommit.UserData(nameValue, emailValue)
            val unixEpoch =
                timestamp.toLongOrNull() ?: raise(Error.DeserializationFailed("Invalid timestamp '$timestamp'"))
            val dateTime = Instant.ofEpochSecond(unixEpoch).atZone(ZoneOffset.of(timezone))
            Pair(user, dateTime)
        } catch (e: Exception) {
            raise(Error.DeserializationFailed("Failed to parse UserData and ZonedDateTime (${e.localizedMessage})"))
        }
    }

    context(RaiseDeserializationFailed)
    private fun getKeyedLines(lines: List<String>): Map<String, List<LineTokens>> =
        lines
            .takeWhile { it.isNotBlank() }
            .mapIndexed { index, line -> Pair(index, line.split(' ').map(String::trim)) }
            .groupBy {
                ensure(it.second.size >= 2) {
                    Error.DeserializationFailed("Line ${it.first} has unexpected content: ${lines[it.first]}")
                }
                it.second.first()
            }
            .mapValues {
                it.value.sortedBy { pair -> pair.first }.map { pair -> pair.second.drop(1) }
            }
}
