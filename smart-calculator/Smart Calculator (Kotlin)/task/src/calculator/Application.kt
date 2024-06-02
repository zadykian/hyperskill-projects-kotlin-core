package calculator

import arrow.core.raise.Raise
import arrow.core.raise.either
import calculator.parser.Error
import calculator.parser.Lexer
import calculator.parser.Parser

class IO(val read: () -> String, val write: (String) -> Unit)

class Application(
    private val calculator: Calculator,
    private val io: IO
) {
    tailrec fun run() {
        val result = either {
            val input = io.read()
            val tokens = Lexer.tokenize(input)
            val command = Parser.parse(tokens)
            handle(command)
            if (command is Command.ExitProgram) return@run
        }

        result.onLeft { io.write(it.displayText) }
        run()
    }

    context(Raise<Error>)
    private fun handle(command: Command) =
        when (command) {
            is Command.EvalExpression -> {
                val expression = calculator.evaluate(command.expression)
                io.write(expression.toString())
            }

            is Command.AssignToIdentifier ->
                calculator.assign(command.identifier, command.expression)

            is Command.DisplayHelp -> io.write(DisplayText.help())
            is Command.ExitProgram -> io.write(DisplayText.exit())
            is Command.Empty -> Unit
        }
}
