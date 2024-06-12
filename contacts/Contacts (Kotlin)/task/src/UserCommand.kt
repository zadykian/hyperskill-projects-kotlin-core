package contacts

enum class UserCommand(val displayName: String) {
    AddRecord("add"),

    RemoveRecord("remove"),

    EditRecord("edit"),

    DisplayRecordsCount("count"),

    ShowRecordInfo("info"),

    ExitProgram("exit");

    companion object {
        private val displayNamesToCommands = entries.associateBy { it.displayName }

        fun getByNameOrNull(name: String) = displayNamesToCommands[name]

        fun getAllNames() = displayNamesToCommands.keys
    }
}
