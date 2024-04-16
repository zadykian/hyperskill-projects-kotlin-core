package gitinternals

import java.nio.file.Files

fun main() {
    val io = object : IO {
        override fun read() = readln()
        override fun write(value: String) = println(value)
    }

    with(io) {
        run()
    }
}

context(IO)
fun run() =
    run {
        write(Requests.GIT_ROOT_DIRECTORY)
        tryGetPath(read())
    }
    .bind {
        if (Files.exists(it)) Success(it) else Failure(Errors.DIRECTORY_NOT_FOUND)
    }
    .bind {
        write(Requests.GIT_OBJECT_HASH)
        val objectHash = read()
        Success(Pair(it, objectHash))
    }
    .bind {
        GitObjectHeaderReader.read(it.first, it.second)
    }
    .bind {
        write(it.toString())
        Success(Unit)
    }
    .onFailure { write(it) }

