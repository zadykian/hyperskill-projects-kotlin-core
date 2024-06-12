package contacts

enum class AppState(val displayName: String, val availableActions: Set<Action>) {
    MainMenu(
        "menu", setOf(
            Action.AddRecord, Action.ListRecords, Action.SearchRecords, Action.DisplayRecordsCount, Action.ExitApp,
        )
    ),
    RecordsList(
        "list", setOf(
            Action.SelectRecordNumber, Action.GoBack,
        )
    ),
    Search(
        "search", setOf(
            Action.SelectRecordNumber, Action.GoBack, Action.SearchAgain,
        )
    ),
    Record(
        "record", setOf(
            Action.EditRecord, Action.DeleteRecord, Action.ReturnToMainMenu
        )
    ),
    Stopped("stopped", emptySet()),
}

enum class Action(val displayName: String, val nextState: AppState) {
    StartApp("start", AppState.MainMenu),

    AddRecord("add", AppState.MainMenu),
    DisplayRecordsCount("count", AppState.MainMenu),

    SearchRecords("search", AppState.Search),
    SearchAgain("again", AppState.Search),
    SelectRecordNumber("[number]", AppState.Record),

    ListRecords("list", AppState.RecordsList),

    EditRecord("edit", AppState.Record),
    DeleteRecord("delete", AppState.MainMenu),

    GoBack("back", AppState.MainMenu),
    ReturnToMainMenu("menu", AppState.MainMenu),
    ExitApp("exit", AppState.Stopped),
}
