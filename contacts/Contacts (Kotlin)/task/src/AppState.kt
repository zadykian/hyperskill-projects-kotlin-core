package contacts

import contacts.domain.PhoneBookEntry

enum class Action(val displayName: String) {
    AddRecord("add"),
    DisplayRecordsCount("count"),
    SearchRecords("search"),
    SearchAgain("again"),
    SelectRecord("[number]"),
    ListRecords("list"),
    EditRecord("edit"),
    DeleteRecord("delete"),
    GoBack("back"),
    ReturnToMainMenu("menu"),
    ExitApp("exit"),
}

sealed class AppState(val displayName: String, val availableActions: Set<Action>) {
    data object MainMenu : AppState(
        "menu", setOf(
            Action.AddRecord, Action.ListRecords, Action.SearchRecords, Action.DisplayRecordsCount, Action.ExitApp,
        )
    )

    interface HasPhoneBookEntries {
        val visibleEntries: List<PhoneBookEntry>
    }

    data class ShowedAllRecords(override val visibleEntries: List<PhoneBookEntry>) : AppState(
        "list", setOf(
            Action.SelectRecord, Action.GoBack,
        )
    ), HasPhoneBookEntries

    data class SearchCompleted(override val visibleEntries: List<PhoneBookEntry>) : AppState(
        "search", setOf(
            Action.SelectRecord, Action.GoBack, Action.SearchAgain,
        )
    ), HasPhoneBookEntries

    data class RecordSelected(val selected: PhoneBookEntry) : AppState(
        "record", setOf(
            Action.EditRecord, Action.DeleteRecord, Action.ReturnToMainMenu
        )
    )

    data object Stopped : AppState("stopped", emptySet())
}

