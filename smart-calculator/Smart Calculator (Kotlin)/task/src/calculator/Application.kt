package calculator

class Application(
    private val commandHandler: CommandHandler,
    private val io: IO
) {
    tailrec fun run() {
        val inputString = io.read()
        val parsed = InputParser.parse(inputString)

        when (parsed) {
            is Input.ArithmeticOperation -> {
                val evaluated = Calculator.evaluate(parsed.expression)
                io.write(evaluated.toString())
            }

            is Input.Command -> commandHandler.handle(parsed.type)
            is Input.Error -> io.write(parsed.type.displayText)
            is Input.Empty -> Unit
        }

        val exit = parsed is Input.Command && parsed.type == CommandType.Exit

        if (!exit) {
            run()
        }
    }
}
