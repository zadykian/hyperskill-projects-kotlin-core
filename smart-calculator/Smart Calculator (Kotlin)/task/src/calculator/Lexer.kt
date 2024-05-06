package calculator

typealias TokenIterator = Iterator<Result<Token>>

typealias TokenSequence = Sequence<Result<Token>>

sealed interface Token {
    class Number(val value: Int) : Token
    class Identifier(val value: calculator.Identifier) : Token
    object Plus : Token
    object Minus : Token
    object Assignment : Token
    object CommandPrefix : Token
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
                char == '/' -> yield(Token.CommandPrefix.success())
                char.isDigit() -> {
                    val uIntVal = advanceWhile { it.isDigit() }.toIntOrNull()
                    yield(uIntVal?.let { Token.Number(it).success() } ?: unexpected())

                    if (uIntVal == null) {
                        return@sequence
                    }
                }

                char.isIdentifierChar() ->
                    advanceWhile { !it.isWhitespace() && !it.isOperator() }
                        .let { Identifier.tryParse(it) }
                        .onSuccess { yield(Token.Identifier(it).success()) }
                        .onFailure {
                            yield(Failure(Errors.INVALID_IDENTIFIER))
                            return@sequence
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
