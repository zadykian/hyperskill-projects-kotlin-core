package calculator.parser

import arrow.core.raise.Raise

sealed interface Token {
    class Number(val value: Int) : Token {
        override fun toString() = value.toString()
    }

    class Word(val value: String) : Token {
        override fun toString() = value
    }

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

    context(Raise<LexerError>)
    fun tokenize(input: CharSequence): List<Token> {
        val tokens = mutableListOf<Token>()

        var index = 0
        while (index <= input.lastIndex) {
            val char = input[index]

            val (nextToken, charsConsumed) = when {
                char.isDigit() -> {
                    val uIntString = input.takeFromWhile(index) { it.isDigit() }
                    Pair(Token.Number(uIntString.toInt()), uIntString.length)
                }

                char.isWordChar() -> {
                    val word = input.takeFromWhile(index) { it.isWordChar() }
                    Pair(Token.Word(word), word.length)
                }

                opTokens.containsKey(char) -> Pair(opTokens.getValue(char), 1)
                char.isWhitespace() -> Pair(null, 1)
                else -> raise(Errors.unexpectedChar(char))
            }

            index += charsConsumed
            nextToken?.let { tokens.add(it) }
        }

        return tokens
    }

    private fun CharSequence.takeFromWhile(startIndex: Int, predicate: (Char) -> Boolean) =
        drop(startIndex).takeWhile(predicate).toString()

    private fun Char.isWordChar() = !this.isWhitespace() && !opTokens.containsKey(this)
}
