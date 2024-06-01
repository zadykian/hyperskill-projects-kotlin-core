package calculator

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
annotation class CommandName(val name: String)

@Target(AnnotationTarget.CLASS)
annotation class CommandDisplayText(val displayText: String)

sealed interface Command {
    @CommandDisplayText("'x + 4 - 1' - evaluate an arithmetic expression")
    data class EvalExpression(val expression: Expression) : Command

    @CommandDisplayText("'x = 2 + 4' - assign the result of the arithmetic expression (2 + 4) to the identifier (x)")
    data class AssignToIdentifier(val identifier: Identifier, val expression: Expression) : Command

    @CommandName("help")
    @CommandDisplayText("display help info")
    data object DisplayHelp : Command

    @CommandName("exit")
    @CommandDisplayText("exit program")
    data object ExitProgram : Command

    data object Empty : Command

    companion object {
        fun nonEmptyClasses() =
            Command::class
                .sealedSubclasses
                .asSequence()
                .filterNot { it.isAbstract || it == Empty::class }
    }
}

fun KClass<out Command>.commandNameOrNull() =
    annotations.filterIsInstance<CommandName>().singleOrNull()?.name

fun KClass<out Command>.commandDisplayText() =
    annotations.filterIsInstance<CommandDisplayText>().singleOrNull()?.displayText
        ?: throw IllegalArgumentException("Display text is not configured for type ${this.qualifiedName}")
