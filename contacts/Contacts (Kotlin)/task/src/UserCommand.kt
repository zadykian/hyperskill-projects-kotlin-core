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

    companion object {
        private val displayNamesToCommands = entries.associateBy {
            it.declaringJavaClass.getField(it.name).getAnnotation(DisplayName::class.java).name
        }

        fun getByNameOrNull(name: String) = displayNamesToCommands[name]

        fun getAllNames() = displayNamesToCommands.keys
    }
}
