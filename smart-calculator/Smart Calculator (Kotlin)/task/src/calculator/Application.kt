package calculator

import arrow.core.raise.either
import calculator.parser.Lexer
import calculator.parser.Parser

class IO(val read: () -> String, val write: (String) -> Unit)

class Application(
    private val calculator: Calculator,
    private val io: IO
) {
    tailrec fun run() {
        either {
            val input = io.read()
            val tokens = Lexer.tokenize(input).bind()
            val command = Parser.parse(tokens).bind()
            handle(command)
            if (command is Command.ExitProgram) return@run
        }

        run()
    }

    private fun handle(command: Command): Any =
        when (command) {
            is Command.EvalExpression -> calculator
                .evaluate(command.expression)
                .onRight { io.write(it.toString()) }
                .onLeft { io.write(it.displayText) }

            is Command.AssignToIdentifier -> calculator
                .assign(command.identifier, command.expression)
                .onLeft { io.write(it.displayText) }

            is Command.DisplayHelp -> io.write(DisplayText.help())
            is Command.ExitProgram -> io.write(DisplayText.exit())
            is Command.Empty -> Unit
        }
}
