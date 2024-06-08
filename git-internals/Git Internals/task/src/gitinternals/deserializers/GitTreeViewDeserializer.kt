package gitinternals.deserializers

import arrow.core.toNonEmptyListOrNull
import gitinternals.Error
import gitinternals.RaiseDeserializationFailed
import gitinternals.objects.GitObjectHash
import gitinternals.objects.GitTreeView
import gitinternals.toNonEmptyStringOrNull
import gitinternals.toStringUtf8

object GitTreeViewDeserializer : GitObjectDeserializer<GitTreeView> {
    private const val SPACE_CODE = '\u0020'.code.toByte()
    private const val NULL_CODE = 0.toByte()

    context(RaiseDeserializationFailed)
    override fun deserialize(objectHash: GitObjectHash, content: ByteArray): GitTreeView {
        fun raise(text: String): Nothing = raise(Error.DeserializationFailed(text))
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
            val nodeHash = GitObjectHash.fromBytesOrNull(objectHashBytes) ?: raise("Tree contains invalid child hash")
            GitTreeView.NodeView(permissionMetadataNumber, nodeHash, objectName)
        }

        val nonEmptyNodes = nodes.toList().toNonEmptyListOrNull() ?: raise("Tree nodes cannot be empty")
        return GitTreeView(objectHash, nonEmptyNodes)
    }

    private fun Iterator<Byte>.takeWhile(predicate: (Byte) -> Boolean): List<Byte> =
        asSequence().takeWhile(predicate).toList()
}
