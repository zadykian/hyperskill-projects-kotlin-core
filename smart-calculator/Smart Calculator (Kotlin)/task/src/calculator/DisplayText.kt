package calculator

object DisplayText {
    fun help() =
        """
        |Welcome to Hyperskill Calculator!
        |Usage:
        |${displayCommands()}
        """.trimMargin()

    private fun displayCommands() =
        Command
            .nonEmptyClasses()
            .map {
                val name = it.commandNameOrNull()
                val displayText = it.commandDisplayText()
                if (name == null) displayText else "/$name - $displayText"
            }
            .sorted()
            .joinToString(separator = System.lineSeparator()) { "    $it" }

    fun exit() = "Bye"
}

