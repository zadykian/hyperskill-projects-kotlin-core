package calculator

private object ExpressionParser {
    fun parse(iterator: TokenIterator): Result<Command.EvalExpression> {
        fun nextRoot(nextToken: Token, currentRoot: Expression) =
            when (nextToken) {
                is Token.Plus -> parseUnary(iterator).map { Expression.Binary(BinaryOp.Addition, currentRoot, it) }
                is Token.Minus -> parseUnary(iterator).map { Expression.Binary(BinaryOp.Subtraction, currentRoot, it) }
                else -> Errors.unexpectedToken(nextToken).failure()
            }

        var root = parseUnary(iterator)

        while (root is Success<Expression> && iterator.hasNext()) {
            root = when (val result = iterator.next()) {
                is Success -> nextRoot(result.value, root.value)
                is Failure -> result.errorText.failure()
            }
        }

        return root.map { Command.EvalExpression(it) }
    }

    private fun parseUnary(iterator: TokenIterator): Result<Expression> =
        iterator
            .nextOrFail()
            .bind { token ->
                when (token) {
                    is Token.Number -> Expression.Number(token.value).success()
                    is Token.Word -> Identifier.tryParse(token.value).map { Expression.Variable(it) }
                    is Token.Plus -> parseUnary(iterator).map { Expression.Unary(UnaryOp.Plus, it) }
                    is Token.Minus -> parseUnary(iterator).map { Expression.Unary(UnaryOp.Negation, it) }
                    else -> Errors.unexpectedToken(token).failure()
                }
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
    // I -> C | A | E | _
    // C -> / ID
    // A -> ID = E
    // E -> U { +|- U }*
    // U -> { +|- }* { { 0..9 }+ | ID }
    // ID -> [a-zA-Z]+

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
