package calculator

typealias TokenIterator = Iterator<Result<Token>>

typealias TokenSequence = Sequence<Result<Token>>

sealed interface Token {
    class Number(val value: Int) : Token
    object Plus : Token
    object Minus : Token
}

object ExpressionLexer {
    fun tokenize(input: String): TokenSequence = sequence {
        var index = 0
        while (index <= input.lastIndex) {
            val char = input[index]

            fun unexpected() = Failure<Token>(DisplayText.Errors.unexpectedChar(char, index))

            when {
                char == '+' -> yield(Token.Plus.success())
                char == '-' -> yield(Token.Minus.success())
                char.isDigit() -> {
                    val uIntVal = generateSequence(char) {
                        if (index < input.lastIndex && input[index + 1].isDigit()) {
                            index++
                            input[index]
                        } else null
                    }.joinToString(separator = "").toIntOrNull()

                    yield(uIntVal?.let { Token.Number(it).success() } ?: unexpected())

                    if (uIntVal == null) {
                        return@sequence
                    }
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
}
