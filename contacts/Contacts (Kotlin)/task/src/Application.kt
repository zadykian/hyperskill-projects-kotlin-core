package contacts

import arrow.core.Either
import arrow.core.Ior
import arrow.core.left
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.right
import contacts.Error.InvalidInput
import contacts.domain.PhoneBook
import contacts.domain.PhoneBookEntry
import contacts.domain.Record
import contacts.dynamic.DynamicObjectFactory
import contacts.dynamic.DynamicStringBuilder
import contacts.dynamic.PropertyName
import kotlin.reflect.KClass

data class IO(val read: () -> String, val write: (CharSequence) -> Unit)

class Application(private val io: IO) {
    private val phoneBook = PhoneBook()

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
        val targetType = requestRecordType()
        val recordIor = DynamicObjectFactory.createNew(targetType) { requestPropertyValue(this) }

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
        val propertyNames = DynamicObjectFactory.propsOf(recordToEdit::class).bind().map { it.displayName }

        io.write(Requests.recordProperty(propertyNames))
        val inputPropName = io.read().lowercase().trim()
        ensure(inputPropName in propertyNames) { Errors.invalidPropName(inputPropName) }

        val editedRecord = DynamicObjectFactory.copy(recordToEdit, inputPropName) { requestPropertyValue(this) }.bind()
        phoneBook.replace(recordToEdit, editedRecord)
        io.write(Responses.recordUpdated())
    }

    private fun requestPropertyValue(context: DynamicObjectFactory.PropOrParamMetadata.PropertyContext): String {
        io.write(Requests.propertyValue(context.propertyName))
        return io.read()
    }

    private fun displayRecordsCount() {
        val count = phoneBook.listAll().size
        io.write(Responses.recordsCount(count))
    }

    context(RaiseAnyError)
    private fun showRecordInfo() {
        val allEntries = phoneBook.listAll()
        val listAsString = allEntries.asBriefList()
        io.write(listAsString)

        io.write(Requests.recordInfoIndex())
        val number = requestNumber(1..allEntries.size).bind()
        val targetEntry = allEntries[number - 1]

        val recordString = DynamicStringBuilder.asString(targetEntry.record)
        io.write(recordString)
        val recordInfoString = DynamicStringBuilder.asString(targetEntry.info)
        io.write(recordInfoString)
    }

    context(RaiseAnyError)
    private fun requestRecordType(): KClass<out Record> {
        val recordCases = DynamicObjectFactory.casesOf<Record>()
        io.write(Requests.recordType(recordCases.map { it.first }))
        val input = io.read().lowercase().trim()
        val targetType = recordCases.find { it.first == input } ?: raise(Errors.unknownRecordType(input))
        return targetType.second
    }

    context(RaiseInvalidInput)
    private fun chooseExistingRecord(currentCommand: UserCommand): Record {
        val allRecords = phoneBook.listAll()
        if (allRecords.isEmpty()) {
            raise(Errors.noRecordsTo(currentCommand))
        }

        io.write(allRecords.asBriefList())
        io.write(Requests.record())

        val recordNumber = requestNumber(1..allRecords.size).bind()
        return allRecords[recordNumber - 1].record
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
        fun command() = "Enter action (${UserCommand.getAllNames().joinToString()}}):"
        fun recordType(names: Iterable<String>) = "Enter the type (${names.joinToString()}):"
        fun propertyValue(propertyName: String) = "Enter the ${propertyName}:"
        fun record() = "Select a record:"
        fun recordProperty(names: Iterable<PropertyName>) = "Select a field (${names.joinToString()}):"
        fun recordInfoIndex() = "Enter index to show info:"
    }

    private object Responses {
        fun commandSeparator() = ""
        fun recordsCount(count: Int) = "The Phone Book has $count records."
        fun recordAdded() = "The record added."
        fun recordRemoved() = "The record removed!"
        fun recordUpdated() = "The record updated!"
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
