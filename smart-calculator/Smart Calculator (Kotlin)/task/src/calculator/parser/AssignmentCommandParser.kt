package calculator.parser

import arrow.core.raise.Raise
import arrow.core.raise.ensure
import calculator.Command
import calculator.Identifier

object AssignmentCommandParser : CommandParser<Command.AssignToIdentifier> {
    override fun canTry(tokens: List<Token>) =
        tokens.isNotEmpty() && tokens.first() != Token.Slash && tokens.contains(Token.Equals)

    context(Raise<ParserError>)
    override fun parse(tokens: List<Token>): Command.AssignToIdentifier {
        ensure(tokens.size >= 3 && tokens[0] is Token.Word && tokens[1] is Token.Equals) {
            Errors.invalidAssignment()
        }

        val identifier = Identifier.tryParse((tokens[0] as Token.Word).value).bind()
        val expression = ExpressionParser.parse(tokens.drop(2))
        return Command.AssignToIdentifier(identifier, expression)
    }
}
