package contacts

import arrow.core.Either
import arrow.core.Ior
import arrow.core.left
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.right
import contacts.Error.InvalidInput
import contacts.dynamic.DynamicObjectFactory
import contacts.dynamic.PropertyName

data class IO(val read: () -> String, val write: (CharSequence) -> Unit)

class Application(private val io: IO) {
    private val phoneBook = PhoneBook()

    tailrec fun run() {
        either {
            val nextCommand = requestNextCommand()
            executeCommand(nextCommand)
            if (nextCommand == UserCommand.ExitProgram) return@run
        }.onLeft { io.write(it.displayText.toString()) }

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
        UserCommand.ListAllRecords -> listAllRecords()
        UserCommand.ExitProgram -> Unit
    }

    context(RaiseAnyError)
    private fun addRecord() {
        val recordIor = DynamicObjectFactory.createNew<Record> { requestPropertyValue(this) }

        val newRecord = when (recordIor) {
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

    context(RaiseInvalidInput)
    private fun removeRecord() {
        val recordToRemove = chooseExistingRecord(UserCommand.RemoveRecord)
        phoneBook.remove(recordToRemove)
        io.write(Responses.recordRemoved())
    }

    context(RaiseAnyError)
    private fun editRecord() {
        val recordToEdit = chooseExistingRecord(UserCommand.EditRecord)
        val propertyNames = DynamicObjectFactory.propertyNamesOf<Record>().bind()

        io.write(Requests.selectRecordProperty(propertyNames))
        val inputPropName = io.read().lowercase().trim()
        ensure(inputPropName in propertyNames) { Errors.invalidPropName(inputPropName) }

        val editedRecord = DynamicObjectFactory.copy(recordToEdit, inputPropName) { requestPropertyValue(this) }.bind()
        phoneBook.replace(recordToEdit, editedRecord)
        io.write(Responses.recordUpdated())
    }

    private fun requestPropertyValue(context: DynamicObjectFactory.Param.PropertyContext): String {
        io.write(Requests.propertyValue(context.propertyName))
        return io.read()
    }

    private fun displayRecordsCount() {
        val count = phoneBook.listAll().size
        io.write(Responses.recordsCount(count))
    }

    private fun listAllRecords() {
        val listAsString = phoneBook.listAll().asString()
        io.write(listAsString)
    }

    context(RaiseInvalidInput)
    private fun chooseExistingRecord(currentCommand: UserCommand): Record {
        val allRecords = phoneBook.listAll()
        if (allRecords.isEmpty()) {
            raise(Errors.noRecordsTo(currentCommand))
        }

        io.write(allRecords.asString())
        io.write(Requests.selectRecord())

        val recordNumber = requestNumber(1..allRecords.size).bind()
        return allRecords[recordNumber - 1]
    }

    private fun requestNumber(range: IntRange): Either<InvalidInput, Int> {
        val input = io.read()
        val number = input.toIntOrNull() ?: return Errors.invalidNumber(input).left()
        return if (number in range) number.right()
        else Errors.numberNotInRange(range).left()
    }

    private fun List<Record>.asString() =
        mapIndexed { index, rec -> "${index + 1}. $rec" }.joinToString(separator = "\n")

    private object Requests {
        fun command() = "Enter action (${UserCommand.getAllNames().joinToString()}}):"
        fun propertyValue(propertyName: String) = "Enter the ${propertyName}:"
        fun selectRecord() = "Select a record:"
        fun selectRecordProperty(names: List<PropertyName>) = "Select a field (${names.joinToString()}):"
    }

    private object Responses {
        fun recordsCount(count: Int) = "The Phone Book has $count records."
        fun recordAdded() = "The record added."
        fun recordRemoved() = "The record removed!"
        fun recordUpdated() = "The record updated!"
    }

    private object Errors {
        fun unknownCommand(input: String) = InvalidInput("Unknown command '$input'")
        fun noRecordsTo(command: UserCommand) = InvalidInput("No records to ${command.displayName}!")
        fun invalidNumber(input: String) = InvalidInput("Invalid number: '$input'")
        fun numberNotInRange(range: IntRange) = InvalidInput("Number should belong to range [$range]")
        fun invalidPropName(input: String) = InvalidInput("Invalid field name '$input'")
    }
}
