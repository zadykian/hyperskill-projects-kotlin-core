package calculator

sealed interface Token {
    data class Number(val value: UInt) : Token
    object Plus : Token
    object Minus : Token
}

object ExpressionLexer {
    fun tokenize(input: String) = sequence {
        var index = 0
        while (index <= input.lastIndex) {
            val char = input[index]
            when {
                char == '+' -> yield(Token.Plus)
                char == '-' -> yield(Token.Minus)
                char.isDigit() -> {
                    val uIntVal = generateSequence(char) {
                        if (index < input.lastIndex && input[index + 1].isDigit()) {
                            index++
                            input[index]
                        } else null
                    }.joinToString(separator = "").toUInt()

                    yield(Token.Number(uIntVal))
                }

                char.isWhitespace() -> Unit
                else -> return@sequence
            }
            index++
        }
    }
}
