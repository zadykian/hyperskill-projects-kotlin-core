package contacts

import contacts.dynamic.annotations.DisplayName

enum class UserCommand {
    @DisplayName("add")
    AddRecord,

    @DisplayName("remove")
    RemoveRecord,

    @DisplayName("edit")
    EditRecord,

    @DisplayName("count")
    DisplayRecordsCount,

    @DisplayName("list")
    ListAllRecords,

    @DisplayName("exit")
    ExitProgram;

    val displayName = this.declaringJavaClass.getField(name).getAnnotation(DisplayName::class.java).name

    companion object {
        private val displayNamesToCommands = entries.associateBy { it.displayName }

        fun getByNameOrNull(name: String) = displayNamesToCommands[name]

        fun getAllNames() = displayNamesToCommands.keys
    }
}
