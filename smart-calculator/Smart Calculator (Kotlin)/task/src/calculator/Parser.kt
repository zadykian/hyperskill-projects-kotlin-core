package calculator

private object ExpressionParser {
    fun parse(iterator: TokenIterator): Result<Input.Expression> {
        fun nextRoot(nextToken: Token, currentRoot: Expression) =
            when (nextToken) {
                is Token.Plus -> parseUnary(iterator).onSuccess { Expression.Binary(Operator.Plus, currentRoot, it) }
                is Token.Minus -> parseUnary(iterator).onSuccess { Expression.Binary(Operator.Minus, currentRoot, it) }
                else -> Errors.unexpectedToken(nextToken).failure()
            }

        var root = parseUnary(iterator)

        while (root is Success<Expression> && iterator.hasNext()) {
            root = when (val result = iterator.next()) {
                is Success -> nextRoot(result.value, root.value)
                is Failure -> result.errorText.failure()
            }
        }

        return root.map { Input.Expression(it) }
    }

    private fun parseUnary(iterator: TokenIterator): Result<Expression> =
        iterator
            .nextOrFail()
            .bind {
                when (it) {
                    is Token.Number -> Expression.Number(it.value).success()
                    is Token.Word -> Identifier.tryParse(it.value).map { Expression.Variable(it) }
                    is Token.Plus -> parseUnary(iterator).map { Expression.Unary(Operator.Plus, it) }
                    is Token.Minus -> parseUnary(iterator).map { Expression.Unary(Operator.Minus, it) }
                    else -> Errors.unexpectedToken(it).failure()
                }
            }
}

private object AssignmentParser {
    fun parse(iterator: TokenIterator): Result<Input.Assignment> =
        iterator
            .nextOf<Token.Word>()
            .bind { Identifier.tryParse(it.value) }
            .bind { id ->
                iterator
                    .nextOf<Token.Assignment>()
                    .bind { ExpressionParser.parse(iterator).mapFailure { Errors.INVALID_ASSIGNMENT } }
                    .map { Input.Assignment(id, it.expression) }
            }
}

private object CommandParser {
    private val commands = CommandType.values().associateBy { it.name.lowercase() }

    fun parse(iterator: TokenIterator): Result<Input.Command> =
        iterator
            .nextOf<Token.Slash>()
            .bind { iterator.nextOf<Token.Word>() }
            .bind {
                commands[it.value.lowercase()]?.success() ?: Errors.UNKNOWN_COMMAND.failure()
            }
            .map { Input.Command(it) }
}

object Parser {
    // I -> C | A | E
    // C -> / ID
    // A -> ID = E
    // E -> U { +|- U }*
    // U -> { +|- }* { { 0..9 }+ | ID }
    // ID -> [a-zA-Z]+

    fun parse(tokens: TokenSequence): Result<Input> {
        val iterator = tokens.iterator()

        val firstTwoTokens = iterator
            .nextOrFail()
            .bind { fstToken -> iterator.nextOrFail().map { sndToken -> Pair(fstToken, sndToken) } }

        return firstTwoTokens.bind { parseTwo(it.first, it.second, iterator) }
    }

    private fun parseTwo(first: Token, second: Token, iterator: TokenIterator): Result<Input> {
        val resetIterator = iterator {
            yield(first.success())
            yield(second.success())
            yieldAll(iterator)
        }

        return when {
            first == Token.Slash -> CommandParser.parse(resetIterator)
            second == Token.Assignment -> AssignmentParser.parse(resetIterator)
            else -> ExpressionParser.parse(resetIterator)
        }
    }
}

private fun <T> Iterator<Result<T>>.nextOrFail() = if (hasNext()) next() else Failure(Errors.UNEXPECTED_EOF)

private inline fun <reified T : Token> TokenIterator.nextOf(): Result<T> =
    if (hasNext()) next().bind {
        if (it is T) Success(it) else Errors.unexpectedToken(it).failure()
    }
    else Errors.UNEXPECTED_EOF.failure()
