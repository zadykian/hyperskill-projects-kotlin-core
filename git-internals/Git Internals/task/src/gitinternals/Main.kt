package gitinternals

import java.nio.file.Files
import java.util.zip.InflaterInputStream

fun main() {
    val io = object : IO {
        override fun read() = readln()
        override fun write(value: String) = println(value)
    }

    io.write(Requests.GIT_OBJECT_LOCATION)
    val path = tryGetPath(io.read())

    if (path == null) {
        io.write(Errors.INVALID_PATH)
        return
    }

    if (Files.exists(path).not()) {
        io.write(Errors.fileDoesntExist(path))
        return
    }

    val decompressedContent = Files
        .newInputStream(path)
        .let { InflaterInputStream(it) }
        .use { stream ->
            stream.reader().use { it.readText() }
        }

    io.write(decompressedContent)
}
