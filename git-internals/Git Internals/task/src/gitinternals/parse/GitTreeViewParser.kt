package gitinternals.parse

import arrow.core.toNonEmptyListOrNull
import gitinternals.*

object GitTreeViewParser : GitObjectParser<GitTreeView> {
    private const val SPACE_CODE = '\u0020'.code.toByte()
    private const val NULL_CODE = 0.toByte()

    context(RaiseParsingFailed)
    override fun parse(content: ByteArray): GitTreeView {
        fun raise(text: String): Nothing = raise(Error.ParsingFailed(text))
        val iterator = content.iterator()

        val nodes = generateSequence {
            if (!iterator.hasNext()) {
                return@generateSequence null
            }

            val permissionMetadataNumber = iterator.takeWhile { it != SPACE_CODE }.toStringUtf8().let {
                it.toUIntOrNull() ?: raise("Invalid permission metadata number '$it'")
            }

            val objectName = iterator.takeWhile { it != NULL_CODE }.toStringUtf8().toNonEmptyStringOrNull()
                ?: raise("Object name cannot be empty")

            val objectHashBytes = iterator.asSequence().take(20).toList()
            val objectHash = GitObjectHash(objectHashBytes).bind()
            GitTreeView.NodeView(permissionMetadataNumber, objectHash, objectName)
        }

        val nonEmptyNodes = nodes.toList().toNonEmptyListOrNull() ?: raise("Tree nodes cannot be empty")
        return GitTreeView(nonEmptyNodes)
    }

    private fun Iterator<Byte>.takeWhile(predicate: (Byte) -> Boolean): List<Byte> =
        asSequence().takeWhile(predicate).toList()
}
