package gitinternals.objects

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import gitinternals.Error

class GitObjectHash private constructor(private val hexValue: String) {
    override fun toString() = hexValue

    companion object {
        private val sha1Regex = "[a-fA-F0-9]{40}".toRegex()
        private val sha256Regex = "[a-fA-F0-9]{64}".toRegex()

        operator fun invoke(gitObjectHash: String): Either<Error.InvalidGitObjectHash, GitObjectHash> =
            if (gitObjectHash.run { matches(sha1Regex) || matches(sha256Regex) })
                GitObjectHash(gitObjectHash.lowercase()).right()
            else Error.InvalidGitObjectHash.left()

        operator fun invoke(bytes: List<Byte>): Either<Error.InvalidGitObjectHash, GitObjectHash> {
            if (bytes.size != 20 && bytes.size != 32) {
                return Error.InvalidGitObjectHash.left()
            }

            val hexString = bytes.joinToString(separator = "") { String.format("%02x", it) }
            return GitObjectHash(hexString).right()
        }
    }
}
