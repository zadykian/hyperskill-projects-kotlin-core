package calculator

class Application(
    private val calculator: Calculator,
    private val io: IO
) {
    tailrec fun run() {
        io.read()
            .let(Lexer::tokenize)
            .let(Parser::parse)
            .onSuccess {
                handle(it)
                if (it is Command.ExitProgram) return
            }
            .onFailure { io.write(it) }

        run()
    }

    private fun handle(command: Command): Any =
        when (command) {
            is Command.EvalExpression -> calculator
                .evaluate(command.expression)
                .onSuccess { io.write(it.toString()) }
                .onFailure { io.write(it) }

            is Command.AssignToIdentifier -> calculator
                .assign(command.identifier, command.expression)
                .onFailure { io.write(it) }

            is Command.DisplayHelp -> io.write(DisplayText.help())
            is Command.ExitProgram -> io.write(DisplayText.exit())
            is Command.Empty -> Unit
        }
}
