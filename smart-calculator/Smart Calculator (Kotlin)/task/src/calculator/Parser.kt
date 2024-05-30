package calculator

private object ExpressionParser {
    val tokensToUnaryOps = mapOf(
        Token.Plus to Operator.Unary.Plus,
        Token.Minus to Operator.Unary.Negation,
    )

    val tokensToBinaryOps = mapOf(
        Token.Plus to Operator.Binary.Addition,
        Token.Minus to Operator.Binary.Subtraction,
        Token.Asterisk to Operator.Binary.Multiplication,
        Token.Slash to Operator.Binary.Division,
        Token.Attic to Operator.Binary.Power,
    )

    fun parse(iterator: TokenIterator): Result<Command.EvalExpression> {
        TODO()
    }

    private fun convertFromInfixToPostfix(tokens: TokenSequence): Result<List<Token>> {
        val operatorsStack = ArrayDeque<Token>()
        val postfixTokens = mutableListOf<Token>()

        for (tokenResult in tokens) {
            tokenResult
                .onSuccess {
                    when (it) {
                        is Token.Number, is Token.Word -> postfixTokens.add(it)
                        Token.Plus -> TODO()

                        Token.Asterisk -> TODO()
                        Token.Attic -> TODO()

                        Token.OpeningParen -> TODO()
                        Token.ClosingParen -> TODO()
                        Token.Equals -> TODO()
                        Token.Minus -> TODO()
                        Token.Slash -> TODO()
                    }
                }
                .onFailure { return it.failure() }
        }

        return postfixTokens.success()
    }
}

private object AssignmentParser {
    fun parse(iterator: TokenIterator): Result<Command.AssignToIdentifier> =
        iterator
            .nextOf<Token.Word>()
            .bind { Identifier.tryParse(it.value) }
            .bind { id ->
                iterator
                    .nextOf<Token.Equals>()
                    .bind { ExpressionParser.parse(iterator).mapFailure { Errors.INVALID_ASSIGNMENT } }
                    .map { Command.AssignToIdentifier(id, it.expression) }
            }
}

private object NamedCommandParser {
    private val namedCommands =
        Command
            .nonEmptyClasses()
            .mapNotNull {
                val name = it.commandNameOrNull()
                if (name == null && it.objectInstance == null) null else Pair(name, it.objectInstance)
            }
            .toMap()

    fun parse(iterator: TokenIterator): Result<Command> =
        iterator
            .nextOf<Token.Slash>()
            .bind { iterator.nextOf<Token.Word>() }
            .bind {
                namedCommands[it.value.lowercase()]?.success() ?: Errors.UNKNOWN_COMMAND.failure()
            }
}

object Parser {
    fun parse(tokens: TokenSequence): Result<Command> {
        val iterator = tokens.iterator()

        val firstTwoTokens = iterator
            .nextOrNull()
            .bind { fstToken -> iterator.nextOrNull().map { Pair(fstToken, it) } }

        return firstTwoTokens.bind { parseTwo(it.first, it.second, iterator) }
    }

    private fun parseTwo(first: Token?, second: Token?, iterator: TokenIterator): Result<Command> {
        val resetIterator = iterator {
            first?.let { yield(it.success()) }
            second?.let { yield(it.success()) }
            yieldAll(iterator)
        }

        return when {
            first == Token.Slash -> NamedCommandParser.parse(resetIterator)
            second == Token.Equals -> AssignmentParser.parse(resetIterator)
            first == null && second == null -> Command.Empty.success()
            else -> ExpressionParser.parse(resetIterator)
        }
    }
}

private fun <T> Iterator<Result<T>>.nextOrNull() = if (hasNext()) next() else Success(null)

private fun <T> Iterator<Result<T>>.nextOrFail() = if (hasNext()) next() else Failure(Errors.UNEXPECTED_EOF)

private inline fun <reified T : Token> TokenIterator.nextOf(): Result<T> =
    if (hasNext()) next().bind {
        if (it is T) Success(it) else Errors.unexpectedToken(it).failure()
    }
    else Errors.UNEXPECTED_EOF.failure()
