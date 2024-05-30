package calculator

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.raise.ensure

typealias ParseError = String

private interface CommandParser<out TCommand : Command> {
    fun canTry(tokens: List<Token>): Boolean
    fun parse(tokens: List<Token>): Either<ParseError, TCommand>
}

private object ExpressionParser : CommandParser<Command.EvalExpression> {
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

    override fun canTry(tokens: List<Token>) = tokens.isNotEmpty() && tokens[0] != Token.Slash

    override fun parse(tokens: List<Token>): Either<ParseError, Command.EvalExpression> {
        TODO()
    }

    private fun convertFromInfixToPostfix(tokens: List<Token>): Result<List<Token>> {
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

private object AssignmentParser : CommandParser<Command.AssignToIdentifier> {
    override fun canTry(tokens: List<Token>) = tokens.size >= 3 && tokens[1] == Token.Equals

    override fun parse(tokens: List<Token>) = either {
        ensure(tokens.size >= 3 && tokens[0] is Token.Word && tokens[1] is Token.Equals) {
            Errors.INVALID_ASSIGNMENT
        }

        val identifier = Identifier.tryParse((tokens[0] as Token.Word).value).bind()
        val expression = ExpressionParser.parse(tokens.drop(2)).bind().expression
        Command.AssignToIdentifier(identifier, expression)
    }
}

private object NamedCommandParser : CommandParser<Command> {
    private val namedCommands =
        Command
            .nonEmptyClasses()
            .mapNotNull {
                val name = it.commandNameOrNull()
                if (name == null && it.objectInstance == null) null else Pair(name, it.objectInstance)
            }
            .toMap()

    override fun canTry(tokens: List<Token>) = tokens.firstOrNull() == Token.Slash

    override fun parse(tokens: List<Token>) = either {
        ensure(tokens.size == 2 && tokens[0] is Token.Slash && tokens[1] is Token.Word) {
            Errors.INVALID_COMMAND_INVOCATION
        }

        val commandName = (tokens[0] as Token.Word).value.lowercase()
        namedCommands[commandName] ?: raise(Errors.UNKNOWN_COMMAND)
    }
}

object Parser {
    private val parsers = listOf(
        NamedCommandParser,
        AssignmentParser,
        ExpressionParser,
    )

    fun parse(tokens: List<Token>): Either<ParseError, Command> =
        parsers
            .firstOrNull { it.canTry(tokens) }
            ?.parse(tokens)
            ?: Errors.INVALID_INPUT.left()
}