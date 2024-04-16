package gitinternals

import java.io.BufferedReader

fun BufferedReader.readWhile(predicate: (Char) -> Boolean): Sequence<Char> = sequence {
    while (true) {
        val char = read()
        if (char == -1 || !predicate(char.toChar())) {
            break
        }
        yield(char.toChar())
    }
}
