package calculator

enum class Operator {
    Plus,
    Minus,
}

sealed interface Expression {
    class Number(val value: UInt) : Expression
    class Unary(val opertr: Operator, val operand: Expression) : Expression
    class Binary(val opertr: Operator, val left: Expression, val right: Expression) : Expression
}

sealed interface Token {
    data class Number(val value: UInt) : Token
    data object Plus : Token
    data object Minus : Token
}

enum class CommandType {
    Help,
    Exit,
}

enum class ErrorType(val displayText: String) {
    UnknownCommand("Unknown command"),
    InvalidExpression("Invalid expression"),
}

sealed interface Input {
    class ArithmeticOperation(val expression: Expression) : Input
    class Command(val type: CommandType) : Input
    class Error(val type: ErrorType) : Input
    object Empty : Input
}

object Calculator {
    fun evaluate(expr: Expression): Int =
        when(expr) {
            is Expression.Number -> expr.value.toInt()
            is Expression.Unary -> when (expr.opertr) {
                Operator.Plus -> evaluate(expr.operand)
                Operator.Minus -> evaluate(expr.operand).unaryMinus()
		    }
            is Expression.Binary -> when (expr.opertr) {
                Operator.Plus -> evaluate(expr.left) + evaluate(expr.right)
                Operator.Minus -> evaluate(expr.left) - evaluate(expr.right)
            }
        }
}

object HelpInfo {
    val displayText =
        """
        Welcome to Hyperskill Calculator!
        Usage:
            [expression] - evaluate an arithmetic expression. Example: 2 + 4 - 1
            /help - display help info
            /exit - exit program
        """.trimIndent()
}

object ExpressionParser {
    // E -> T { +|- T }*
    // T -> { +|- }* { 0..9 }+
    
    fun parse(tokens: Sequence<Token>): Expression? {
        val iterator = tokens.iterator()
        var root = parseUnary(iterator)

        while (root != null && iterator.hasNext()) {
            root = when (iterator.next()) {
                is Token.Plus -> parseUnary(iterator)?.let {
                    Expression.Binary(Operator.Plus, root!!, it)
                }
                is Token.Minus -> parseUnary(iterator)?.let {
                    Expression.Binary(Operator.Minus, root!!, it)
                }
                else -> null
            }
        }

        return root
    }

    private fun parseUnary(iterator: Iterator<Token>) : Expression? {
        if (iterator.hasNext().not()) {
            return null
        }

        val nextToken = iterator.next()

        return when (nextToken) {
            is Token.Plus -> parseUnary(iterator)?.let {
                Expression.Unary(Operator.Plus, it)
            }
            is Token.Minus -> parseUnary(iterator)?.let {
                Expression.Unary(Operator.Minus, it)
            }
            is Token.Number -> Expression.Number(nextToken.value)
        }
    }
}

object ExpressionLexer {
    fun tokenize(input: String) = sequence {
        var index = 0
        while (index <= input.lastIndex) {
            val char = input[index]
            when {
                char == '+' -> yield(Token.Plus)
                char == '-' -> yield(Token.Minus)
                char.isDigit() -> {
                    val uIntVal = generateSequence(char) {
                        if (index < input.lastIndex && input[index + 1].isDigit()) {
                            index++
                            input[index]
                        } else null
                    }.joinToString(separator = "").toUInt()

                    yield(Token.Number(uIntVal))
                }
                char.isWhitespace() -> Unit
                else -> return@sequence
            }
            index++
        }
    }
}

object InputParser {
    private val commands = CommandType.entries.associateBy { it.name.lowercase() }

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

tailrec fun run(): Unit {
    val inputString = readln()
    val parsed = InputParser.parse(inputString)

    when (parsed) {
        is Input.ArithmeticOperation -> Calculator.evaluate(parsed.expression).let { println(it) }
        is Input.Command -> when (parsed.type) {
            CommandType.Help -> println(HelpInfo.displayText)
            CommandType.Exit -> println("Bye!")
        }
        is Input.Error -> println(parsed.type.displayText)
        is Input.Empty -> Unit
    }

    val exit = parsed is Input.Command && parsed.type == CommandType.Exit

    if (!exit) {
        run()
    }
}

fun main() = run()