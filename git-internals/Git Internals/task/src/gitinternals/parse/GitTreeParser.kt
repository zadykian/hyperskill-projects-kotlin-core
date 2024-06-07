package gitinternals.parse

import arrow.core.toNonEmptyListOrNull
import gitinternals.*

object GitTreeParser : GitObjectParser<Tree> {
    private const val SPACE_CODE = '\u0020'.code.toByte()
    private const val NULL_CODE = 0.toByte()

    context(RaiseParsingFailed)
    override fun parse(content: ByteArray): Tree {
        fun raise(text: String): Nothing = raise(Error.ParsingFailed(text))
        val iterator = content.iterator()

        val nodes = generateSequence {
            if (!iterator.hasNext()) {
                return@generateSequence null
            }

            val permissionMetadataNumber = iterator.takeWhile { it != SPACE_CODE }.toStringUtf8().let {
                it.toUIntOrNull() ?: raise("Invalid permission metadata number '$it'")
            }

            val fileName = iterator.takeWhile { it != NULL_CODE }.toStringUtf8().toNonEmptyStringOrNull()
                ?: raise("File name cannot be empty")

            val fileHashBytes = iterator.asSequence().take(20).toList()
            val fileHash = GitObjectHash(fileHashBytes).bind()

            Tree.Node(permissionMetadataNumber, fileHash, fileName)
        }

        val nonEmptyNodes = nodes.toList().toNonEmptyListOrNull() ?: raise("Tree nodes cannot be empty")
        return Tree(nonEmptyNodes)
    }

    private fun Iterator<Byte>.takeWhile(predicate: (Byte) -> Boolean): List<Byte> =
        asSequence().takeWhile(predicate).toList()
}
