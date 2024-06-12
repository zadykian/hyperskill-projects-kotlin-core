package contacts

import arrow.core.Either
import arrow.core.Ior
import arrow.core.raise.either
import arrow.core.raise.ior
import contacts.Error.ApplicationError
import contacts.Error.InvalidInput
import contacts.domain.*

data class IO(val read: () -> String, val write: (CharSequence) -> Unit)

class Application(private val phoneBook: PhoneBook, private val io: IO) {
    private val recordNamesToFactories = mapOf(
        "person" to {
            ior(Error::combine) {
                val properties = requestPropertyValues<Person.Property>().bind()
                Person(properties).bind()
            }
        },
        "organization" to {
            ior(Error::combine) {
                val properties = requestPropertyValues<Organization.Property>().bind()
                Organization(properties).bind()
            }
        }
    )

    fun run() {
        val statesSequence = generateSequence<AppState>(AppState.MainMenu) {
            when (val next = either { runIteration(it) }) {
                is Either.Right -> next.value
                is Either.Left -> {
                    io.write(next.value.displayText)
                    AppState.MainMenu
                }
            }
        }

        statesSequence.firstOrNull { it is AppState.Stopped }
    }

    context(RaiseAnyError)
    private fun runIteration(currentState: AppState): AppState {
        if (currentState.availableActions.isEmpty()) {
            return AppState.Stopped
        }

        val (nextAction, input) = selectNextAction(currentState)

        val nextState = when (nextAction) {
            Action.AddRecord -> addRecord()
            Action.DisplayRecordsCount -> displayRecordsCount()
            Action.SearchRecords,
            Action.SearchAgain -> executeSearchQuery()

            Action.SelectRecord -> selectRecord(currentState, input)
            Action.ListRecords -> listRecords()
            Action.EditRecord -> editRecord(currentState)
            Action.DeleteRecord -> deleteRecord(currentState)

            Action.GoBack,
            Action.ReturnToMainMenu -> AppState.MainMenu

            Action.ExitApp -> AppState.Stopped
        }

        io.write(Responses.actionSeparator())
        return nextState
    }

    context(RaiseInvalidInput)
    private fun selectNextAction(state: AppState): Pair<Action, String> {
        val actionNames = state.availableActions.associateBy { it.displayName.trim() }
        io.write(Requests.action(state.displayName, actionNames.keys))
        val input = io.read().trim().lowercase()

        if (Action.SelectRecord in state.availableActions
            && input.toIntOrNull() != null
        ) {
            return Pair(Action.SelectRecord, input)
        }

        return actionNames[input]?.let { Pair(it, input) } ?: raise(Errors.unknownAction(input))
    }

    private fun listRecords(): AppState {
        val allEntries = phoneBook.listAll()

        if (allEntries.isEmpty()) {
            io.write(Responses.recordsCount(0))
            return AppState.MainMenu
        }

        io.write(allEntries.asBriefList())
        return AppState.ShowedAllRecords(allEntries)
    }

    context(RaiseAnyError)
    private fun addRecord(): AppState {
        val recordCases = recordNamesToFactories.keys
        io.write(Requests.recordType(recordCases))
        val input = io.read().lowercase().trim()
        val recordFactory = recordNamesToFactories[input] ?: raise(Errors.unknownRecordType(input))

        val newRecord = when (val recordIor = recordFactory()) {
            is Ior.Right -> recordIor.value
            is Ior.Both -> {
                io.write(recordIor.leftValue.displayText)
                recordIor.rightValue
            }

            is Ior.Left -> raise(recordIor.value)
        }

        phoneBook.add(newRecord)
        io.write(Responses.recordAdded())
        return AppState.MainMenu
    }

    private inline fun <reified T> requestPropertyValues() where T : RecordProperty, T : Enum<T> =
        ior(Error::combine) {
            enumValues<T>()
                .map { enum -> consumePropertyFromUser<T>(enum).map { Pair(enum, it) } }
                .bindAll()
                .toMap()
        }

    private inline fun <reified T> consumePropertyFromUser(
        property: T
    ): Ior<InvalidInput, Any?> where T : RecordProperty {
        io.write(Requests.propertyValue(property.displayName))
        val input = io.read()
        return when (val parsed = property.parser(input)) {
            is Either.Right -> Ior.Right(parsed.value)
            is Either.Left -> Ior.Both(Warnings.invalidProperty(property.displayName), null)
        }
    }

    context(RaiseInvalidInput)
    private fun executeSearchQuery(): AppState {
        io.write(Requests.searchQuery())
        val searchQuery = NonEmptyString(io.read()).bind()
        val searchResults = phoneBook.find(searchQuery)
        io.write(Responses.foundResults(searchResults.size))
        io.write(searchResults.asBriefList())
        return AppState.SearchCompleted(searchResults)
    }

    context(RaiseAnyError)
    private fun selectRecord(
        currentState: AppState,
        input: String
    ): AppState {
        if (currentState !is AppState.HasPhoneBookEntries) {
            raise(Errors.invalidAction(Action.SelectRecord, currentState))
        }

        val number = input.toIntOrNull() ?: raise(Errors.invalidNumber(input))

        val range = 1..currentState.visibleEntries.size
        val recordNumber = if (number in range) number else raise(Errors.numberNotInRange(range))

        val selectedEntry = currentState.visibleEntries[recordNumber - 1]
        io.write(selectedEntry.toString())
        return AppState.RecordSelected(selectedEntry)
    }

    context(RaiseAnyError)
    private fun deleteRecord(currentState: AppState): AppState {
        if (currentState !is AppState.RecordSelected) {
            raise(Errors.invalidAction(Action.DeleteRecord, currentState))
        }

        val recordToRemove = currentState.selected.record
        phoneBook.remove(recordToRemove)
        io.write(Responses.recordRemoved())
        return AppState.MainMenu
    }

    context(RaiseAnyError)
    private fun editRecord(currentState: AppState): AppState {
        if (currentState !is AppState.RecordSelected) {
            raise(Errors.invalidAction(Action.DeleteRecord, currentState))
        }

        val recordToEdit = currentState.selected.record

        val editedRecord = when (recordToEdit) {
            is Person -> Person(getUpdatedProps(recordToEdit)).bind()
            is Organization -> Organization(getUpdatedProps(recordToEdit)).bind()
        }

        val newEntry = phoneBook.replace(recordToEdit, editedRecord)
        io.write(Responses.recordUpdated())
        io.write(newEntry.toString())

        return AppState.RecordSelected(newEntry)
    }

    context(RaiseInvalidInput)
    private inline fun <reified T> getUpdatedProps(recordToEdit: Record<T>): Properties<T>
            where T : RecordProperty, T : Enum<T> {
        val byDisplayName = enumValues<T>().associateBy { it.displayName }
        io.write(Requests.recordProperty(byDisplayName.keys))
        val inputPropName = io.read().lowercase().trim()
        val property = byDisplayName[inputPropName] ?: raise(Errors.invalidPropName(inputPropName))

        val newPropertyValue = when (val propIor = consumePropertyFromUser(property)) {
            is Ior.Right -> propIor.value
            is Ior.Both -> {
                io.write(propIor.leftValue.displayText)
                propIor.rightValue
            }

            is Ior.Left -> raise(propIor.value)
        }

        return recordToEdit.properties + (property to newPropertyValue)
    }

    private fun displayRecordsCount(): AppState {
        val count = phoneBook.listAll().size
        io.write(Responses.recordsCount(count))
        return AppState.MainMenu
    }

    private fun List<PhoneBookEntry>.asBriefList() =
        mapIndexed { index, rec -> "${index + 1}. ${rec.record.toStringShort()}" }.joinToString(separator = "\n")

    private object Requests {
        fun action(stateName: String, actionNames: Iterable<String>) =
            "[$stateName] Enter action (${actionNames.joinToString()}):"

        fun searchQuery() = "Enter search query:"

        fun recordType(names: Iterable<String>) = "Enter the type (${names.joinToString()}):"
        fun propertyValue(propertyName: String) = "Enter the ${propertyName}:"
        fun recordProperty(names: Iterable<String>) = "Select a field (${names.joinToString()}):"
    }

    private object Responses {
        fun actionSeparator() = ""
        fun recordsCount(count: Int) = "The Phone Book has $count records."
        fun recordAdded() = "The record added."
        fun recordUpdated() = "Saved"
        fun recordRemoved() = "The record removed"
        fun foundResults(count: Int) = "Found $count results:"
    }

    private object Warnings {
        fun invalidProperty(propertyName: String) = InvalidInput("Bad $propertyName!")
    }

    private object Errors {
        fun unknownAction(input: String) = InvalidInput("Unknown action '$input'")
        fun invalidAction(action: Action, currentState: AppState) =
            ApplicationError("Invalid action '$action' for current state '$currentState'")

        fun unknownRecordType(input: String) = InvalidInput("Unknown record type '$input'")
        fun invalidNumber(input: String) = InvalidInput("Invalid number: '$input'")
        fun numberNotInRange(range: IntRange) = InvalidInput("Number should belong to range [$range]")
        fun invalidPropName(input: String) = InvalidInput("Invalid field name '$input'")
    }
}
