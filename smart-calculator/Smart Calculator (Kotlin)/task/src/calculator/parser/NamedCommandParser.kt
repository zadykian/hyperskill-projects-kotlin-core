package calculator.parser

import arrow.core.raise.Raise
import arrow.core.raise.ensure
import calculator.Command
import calculator.commandNameOrNull

object NamedCommandParser : CommandParser<Command> {
    private val namedCommands =
        Command
            .nonEmptyClasses()
            .mapNotNull {
                val name = it.commandNameOrNull()
                if (name == null && it.objectInstance == null) null else Pair(name, it.objectInstance)
            }
            .toMap()

    override fun canTry(tokens: List<Token>) = tokens.firstOrNull() == Token.Slash

    context(Raise<ParserError>)
    override fun parse(tokens: List<Token>): Command {
        ensure(tokens.size == 2 && tokens[0] is Token.Slash && tokens[1] is Token.Word) {
            Errors.invalidCommandInvocation()
        }

        val commandName = (tokens[1] as Token.Word).value.lowercase()
        return namedCommands[commandName] ?: raise(Errors.unknownCommand())
    }
}
