package calculator

typealias TokenIterator = Iterator<Result<Token>>

typealias TokenSequence = Sequence<Result<Token>>

sealed interface Token {
    class Number(val value: Int) : Token
    class Word(val value: String) : Token
    data object Plus : Token
    data object Minus : Token
    data object Asterisk : Token
    data object Slash : Token
    data object Attic : Token
    data object Equals : Token
    data object OpeningParen : Token
    data object ClosingParen : Token
}

object Lexer {
    private val opTokens = mapOf(
        '+' to Token.Plus,
        '-' to Token.Minus,
        '*' to Token.Asterisk,
        '/' to Token.Slash,
        '^' to Token.Attic,
        '=' to Token.Equals,
        '(' to Token.OpeningParen,
        ')' to Token.ClosingParen,
    )

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
                char.isDigit() -> {
                    val uIntVal = advanceWhile { it.isDigit() }.toIntOrNull()
                    yield(uIntVal?.let { Token.Number(it).success() } ?: unexpected())
                    if (uIntVal == null) return@sequence
                }

                char.isWordChar() -> {
                    val word = advanceWhile { it.isWordChar() }
                    yield(Token.Word(word).success())
                }

                opTokens.containsKey(char) -> yield(opTokens.getValue(char).success())

                char.isWhitespace() -> Unit

                else -> {
                    yield(unexpected())
                    return@sequence
                }
            }
            index++
        }
    }

    private fun Char.isWordChar() = !this.isWhitespace() && !this.isOperator()

    private fun Char.isOperator() = this == '+' || this == '-' || this == '=' || this == '/'
}
