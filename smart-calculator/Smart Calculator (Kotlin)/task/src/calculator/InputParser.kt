package calculator

object InputParser {
    private val commands = CommandType.values().associateBy { it.name.lowercase() }

    fun parse(input: String): Input = input
        .trim()
        .lowercase()
        .let {
            when {
                it.isEmpty() -> Input.Empty
                it[0] == '/' -> it
                    .removePrefix("/")
                    .let { cmd -> commands[cmd] }
                    ?.let(Input::Command)
                    ?: Input.Error(ErrorType.UnknownCommand)

                else -> it
                    .let(ExpressionLexer::tokenize)
                    .let(ExpressionParser::parse)
                    ?.let(Input::ArithmeticOperation)
                    ?: Input.Error(ErrorType.InvalidExpression)
            }
        }
}
