package gitinternals

import java.time.format.DateTimeFormatter

val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxx")

fun <T> StringBuilder.appendLine(vararg values: T): StringBuilder = append(*values).append("\n")

fun List<Byte>.toStringUtf8(): String = String(this.toByteArray(), Charsets.UTF_8)

fun ByteArray.toStringUtf8(): String = String(this, Charsets.UTF_8)