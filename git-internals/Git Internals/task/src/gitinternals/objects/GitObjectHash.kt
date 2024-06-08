package gitinternals.objects

class GitObjectHash private constructor(private val hexValue: String) {
    override fun toString() = hexValue

    companion object {
        private val sha1Regex = "[a-fA-F0-9]{40}".toRegex()
        private val sha256Regex = "[a-fA-F0-9]{64}".toRegex()

        fun fromStringOrNull(gitObjectHash: String): GitObjectHash? =
            if (gitObjectHash.run { matches(sha1Regex) || matches(sha256Regex) })
                GitObjectHash(gitObjectHash.lowercase())
            else null

        fun fromBytesOrNull(bytes: List<Byte>): GitObjectHash? {
            if (bytes.size != 20 && bytes.size != 32) {
                return null
            }

            val hexString = bytes.joinToString(separator = "") { String.format("%02x", it) }
            return fromStringOrNull(hexString)
        }
    }
}
