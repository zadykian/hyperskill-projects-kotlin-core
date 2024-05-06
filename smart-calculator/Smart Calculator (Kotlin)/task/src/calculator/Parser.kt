package calculator

//private val commandPattern = Regex("^\\s*/(?<cmd>[a-zA-Z]+)\\s*$")
//private val commands = CommandType.values().associateBy { it.name.lowercase() }
//
//fun parse(input: String): Result<Input> =
//    when {
//        input.isBlank() -> Input.Empty.success()
//
//        commandPattern.matches(input) -> {
//            val commandName = commandPattern.matchEntire(input)!!.groups["cmd"]!!.value.lowercase()
//            val command = commands[commandName]
//            command?.let { cmd -> Input.Command(cmd).success() } ?: Failure(Errors.UNKNOWN_COMMAND)
//        }
//
//        else -> {
//            val parsed = Lexer.tokenize(input).let { ExpressionParser.parse(it) }
//            parsed.map { expr -> Input.Expression(expr) }
//        }
//    }

object Parser {
    // E -> '/'ID
    // E -> T { +|- T }*
    // T -> { +|- }* { 0..9 }+
    // ID -> [a-zA-Z]+

    fun parse(tokens: TokenSequence): Result<Input> {
        val iterator = tokens.iterator()
        var root = parseUnary(iterator)

        while (root != null && iterator.hasNext()) {
            root = when (iterator.next()) {
                is Token.Plus -> parseUnary(iterator).onSuccess {
                    Expression.Binary(Operator.Plus, root, it)
                }

                is Token.Minus -> parseUnary(iterator).onSuccess {
                    Expression.Binary(Operator.Minus, root, it)
                }

                else -> null
            }
        }

        return root
    }

    private fun parseUnary(iterator: TokenIterator): Result<Expression> =
        when (val nextResult = iterator.nextOrNull()) {
            is Success -> when (nextResult.value) {
                is Token.Number -> Expression.Number(nextResult.value.value).success()
                is Token.Identifier -> Expression.Variable(nextResult.value.value).success()
                is Token.Plus -> parseUnary(iterator).map { Expression.Unary(Operator.Plus, it) }
                is Token.Minus -> parseUnary(iterator).map { Expression.Unary(Operator.Minus, it) }
            }

            is Failure -> Failure(nextResult.errorText)
            null -> Failure(Errors.UNEXPECTED_EOF)
        }

    private fun <T> Iterator<T>.nextOrNull() = if (hasNext()) next() else null
}
