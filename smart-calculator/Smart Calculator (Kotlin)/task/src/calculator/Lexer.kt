package calculator

typealias TokenIterator = Iterator<Result<Token>>

typealias TokenSequence = Sequence<Result<Token>>

sealed interface Token {
    class Number(val value: Int) : Token
    class Word(val value: String) : Token
    data object Plus : Token
    data object Minus : Token
    data object Assignment : Token
    data object Slash : Token
}

object Lexer {
    fun tokenize(input: String): TokenSequence = sequence {
        var index = 0
        while (index <= input.lastIndex) {
            val char = input[index]

            fun unexpected() = Failure<Token>(Errors.unexpectedChar(char, index))

            fun advanceWhile(predicate: (Char) -> Boolean) = generateSequence(char) {
                if (index < input.lastIndex && predicate(input[index + 1])) {
                    index++
                    input[index]
                } else null
            }.joinToString(separator = "")

            when {
                char == '+' -> yield(Token.Plus.success())
                char == '-' -> yield(Token.Minus.success())
                char == '=' -> yield(Token.Assignment.success())
                char == '/' -> yield(Token.Slash.success())
                char.isDigit() -> {
                    val uIntVal = advanceWhile { it.isDigit() }.toIntOrNull()
                    yield(uIntVal?.let { Token.Number(it).success() } ?: unexpected())

                    if (uIntVal == null) {
                        return@sequence
                    }
                }

                char.isIdentifierChar() -> {
                    val word = advanceWhile { !it.isWhitespace() && !it.isOperator() }
                    yield(Token.Word(word).success())
                }

                char.isWhitespace() -> Unit

                else -> {
                    yield(unexpected())
                    return@sequence
                }
            }
            index++
        }
    }

    private fun Char.isIdentifierChar() = this in 'a'..'z' || this in 'A'..'Z'

    private fun Char.isOperator() = this == '+' || this == '-' || this == '=' || this == '/'
}
