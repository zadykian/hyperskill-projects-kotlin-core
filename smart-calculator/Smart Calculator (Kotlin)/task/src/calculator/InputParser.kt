package calculator

object InputParser {
    private val commandPattern = Regex("^\\s*/(?<cmd>[a-zA-Z]+)\\s*$")
    private val commands = CommandType.values().associateBy { it.name.lowercase() }

    fun parse(input: String): Result<Input> =
        when {
            input.isBlank() -> Input.Empty.success()

            commandPattern.matches(input) -> {
                val commandName = commandPattern.matchEntire(input)!!.groups["cmd"]!!.value.lowercase()
                val command = commands[commandName]
                command?.let { cmd -> Input.Command(cmd).success() } ?: Failure(DisplayText.Errors.UNKNOWN_COMMAND)
            }

            else -> {
                val parsed = ExpressionLexer.tokenize(input).let { ExpressionParser.parse(it) }
                parsed.map { expr -> Input.ArithmeticExpression(expr) }
            }
        }
}
