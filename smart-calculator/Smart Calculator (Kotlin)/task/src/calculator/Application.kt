package calculator

import arrow.core.raise.Raise
import arrow.core.raise.either
import calculator.parser.Error
import calculator.parser.Lexer
import calculator.parser.Parser
import kotlinx.coroutines.runBlocking

class IO(val read: () -> String, val write: (String) -> Unit)

class Application(
    private val calculator: Calculator,
    private val io: IO
) {
    tailrec fun run() {
        runBlocking { }

        either {
            val input = io.read()
            val tokens = Lexer.tokenize(input)
            val command = Parser.parse(tokens)
            handle(command)
            if (command is Command.ExitProgram) return@run
        }

        run()
    }

    context(Raise<Error>)
    private fun handle(command: Command): Any =
        when (command) {
            is Command.EvalExpression ->
                either { calculator.evaluate(command.expression) }
                    .onRight { io.write(it.toString()) }
                    .onLeft { io.write(it.displayText) }

            is Command.AssignToIdentifier ->
                either { calculator.assign(command.identifier, command.expression) }
                    .onLeft { io.write(it.displayText) }

            is Command.DisplayHelp -> io.write(DisplayText.help())
            is Command.ExitProgram -> io.write(DisplayText.exit())
            is Command.Empty -> Unit
        }
}
