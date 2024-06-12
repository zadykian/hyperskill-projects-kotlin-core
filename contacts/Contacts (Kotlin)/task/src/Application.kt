package contacts

import arrow.core.Either
import arrow.core.Ior
import arrow.core.left
import arrow.core.raise.either
import arrow.core.raise.ior
import arrow.core.right
import contacts.Error.InvalidInput
import contacts.domain.*

data class IO(val read: () -> String, val write: (CharSequence) -> Unit)

class Application(private val io: IO) {
    private val phoneBook = PhoneBook()

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

    tailrec fun run() {
        either {
            val nextCommand = requestNextCommand()
            executeCommand(nextCommand)
            if (nextCommand == UserCommand.ExitProgram) return@run
        }.onLeft { io.write(it.displayText.toString()) }

        io.write(Responses.commandSeparator())
        run()
    }

    context(RaiseInvalidInput)
    private fun requestNextCommand(): UserCommand {
        io.write(Requests.command())
        val input = io.read()
        return UserCommand.getByNameOrNull(input) ?: raise(Errors.unknownCommand(input))
    }

    context(RaiseAnyError)
    private fun executeCommand(command: UserCommand) = when (command) {
        UserCommand.AddRecord -> addRecord()
        UserCommand.RemoveRecord -> removeRecord()
        UserCommand.EditRecord -> editRecord()
        UserCommand.DisplayRecordsCount -> displayRecordsCount()
        UserCommand.ShowRecordInfo -> showRecordInfo()
        UserCommand.ExitProgram -> Unit
    }

    context(RaiseAnyError)
    private fun addRecord() {
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
    private fun removeRecord() {
        val recordToRemove = chooseExistingEntry(UserCommand.RemoveRecord).record
        phoneBook.remove(recordToRemove)
        io.write(Responses.recordRemoved())
    }

    context(RaiseInvalidInput)
    private fun editRecord() {
        val recordToEdit = chooseExistingEntry(UserCommand.EditRecord).record

        val editedRecord = when (recordToEdit) {
            is Person -> Person(getUpdatedProps(recordToEdit)).bind()
            is Organization -> Organization(getUpdatedProps(recordToEdit)).bind()
        }

        phoneBook.replace(recordToEdit, editedRecord)
        io.write(Responses.recordUpdated())
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

    private fun displayRecordsCount() {
        val count = phoneBook.listAll().size
        io.write(Responses.recordsCount(count))
    }

    context(RaiseAnyError)
    private fun showRecordInfo() {
        val entry = chooseExistingEntry(UserCommand.ShowRecordInfo)
        io.write(entry.toString())
    }

    context(RaiseInvalidInput)
    private fun chooseExistingEntry(currentCommand: UserCommand): PhoneBookEntry {
        val allEntries = phoneBook.listAll()
        if (allEntries.isEmpty()) {
            raise(Errors.noRecordsTo(currentCommand))
        }

        io.write(allEntries.asBriefList())
        io.write(Requests.record())

        val entryNumber = requestNumber(1..allEntries.size).bind()
        return allEntries[entryNumber - 1]
    }

    private fun requestNumber(range: IntRange): Either<InvalidInput, Int> {
        val input = io.read()
        val number = input.toIntOrNull() ?: return Errors.invalidNumber(input).left()
        return if (number in range) number.right()
        else Errors.numberNotInRange(range).left()
    }

    private fun List<PhoneBookEntry>.asBriefList() =
        mapIndexed { index, rec -> "${index + 1}. ${rec.record.toStringShort()}" }.joinToString(separator = "\n")

    private object Requests {
        fun command() = "Enter action (${UserCommand.getAllNames().joinToString()}):"
        fun recordType(names: Iterable<String>) = "Enter the type (${names.joinToString()}):"
        fun propertyValue(propertyName: String) = "Enter the ${propertyName}:"
        fun record() = "Select a record:"
        fun recordProperty(names: Iterable<String>) = "Select a field (${names.joinToString()}):"
    }

    private object Responses {
        fun commandSeparator() = ""
        fun recordsCount(count: Int) = "The Phone Book has $count records."
        fun recordAdded() = "The record added."
        fun recordRemoved() = "The record removed!"
        fun recordUpdated() = "The record updated!"
    }

    private object Warnings {
        fun invalidProperty(propertyName: String) = InvalidInput("Bad $propertyName!")
    }

    private object Errors {
        fun unknownCommand(input: String) = InvalidInput("Unknown command '$input'")
        fun unknownRecordType(input: String) = InvalidInput("Unknown record type '$input'")
        fun noRecordsTo(command: UserCommand) = InvalidInput("No records to ${command.displayName}!")
        fun invalidNumber(input: String) = InvalidInput("Invalid number: '$input'")
        fun numberNotInRange(range: IntRange) = InvalidInput("Number should belong to range [$range]")
        fun invalidPropName(input: String) = InvalidInput("Invalid field name '$input'")
    }
}
