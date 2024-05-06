package calculator

class Application(
    private val calculator: Calculator,
    private val io: IO
) {
    tailrec fun run() {
        val runNext = when (val parsed = InputParser.parse(io.read())) {
            is Success -> {
                handleInput(parsed.value)
                !parsed.value.isExitCommand()
            }

            is Failure -> {
                io.write(parsed.errorText)
                true
            }
        }

        if (runNext) {
            run()
        }
    }

    private fun handleInput(input: Input): Any =
        when (input) {
            is Input.ArithmeticExpression -> calculator
                .evaluate(input.expression)
                .onSuccess { io.write(it.toString()) }
                .onFailure { io.write(it) }

            is Input.VariableAssigment -> calculator
                .assign(input.assignment)
                .onFailure { io.write(it) }

            is Input.Command -> handleCommand(input.type)
            is Input.Empty -> Unit
        }

    private fun handleCommand(commandType: CommandType) =
        when (commandType) {
            CommandType.Help -> io.write(DisplayText.helpText)
            CommandType.Exit -> io.write(DisplayText.EXIT_TEXT)
        }

    private fun Input.isExitCommand() = this is Input.Command && type == CommandType.Exit
}
