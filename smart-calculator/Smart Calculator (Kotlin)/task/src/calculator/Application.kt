package calculator

class Application(
    private val calculator: Calculator,
    private val io: IO
) {
    tailrec fun run() {
        val inputString = io.read()
        val parsed = InputParser.parse(inputString)

        when (parsed) {
            is Input.ArithmeticExpression -> calculator.evaluate(parsed.expression).onFailure { io.write(it) }

            is Input.VariableAssigment -> calculator.assign(parsed.assignment).onFailure { io.write(it) }

            is Input.Command -> when (parsed.type) {
                CommandType.Help -> io.write(DisplayText.helpText)
                CommandType.Exit -> io.write(DisplayText.EXIT_TEXT)
            }

            is Input.Error -> io.write(parsed.type.displayText)
            is Input.Empty -> Unit
        }

        val exit = parsed is Input.Command && parsed.type == CommandType.Exit

        if (!exit) {
            run()
        }
    }
}
