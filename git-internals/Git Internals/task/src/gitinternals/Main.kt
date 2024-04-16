package gitinternals

import java.nio.file.Files
import java.nio.file.Paths

fun main() = with(IO(::readln, ::println)) {
    run {
        write(Requests.GIT_ROOT_DIRECTORY)
        val path = Paths.get(read())
        if (Files.exists(path)) Success(path) else Failure(Errors.DIRECTORY_NOT_FOUND)
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
}
