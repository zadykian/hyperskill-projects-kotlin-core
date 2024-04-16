package gitinternals

import java.nio.file.Files
import java.util.zip.InflaterInputStream

fun main() {
    val io = object : IO {
        override fun read() = readln()
        override fun write(value: String) = println(value)
    }

    io.write(Requests.GIT_ROOT_DIRECTORY)
    val path = tryGetPath(io.read())




    //io.write(splitByNullChar)
}
