package contacts

import arrow.core.raise.either

data class IO(val read: () -> String, val write: (String) -> Unit)

class Application(private val io: IO) {
    private val records = mutableListOf<Record>()

    fun run() {
        either { addContact() }.onLeft { io.write(it.displayText.toString()) }
    }

    context(RaiseAnyError)
    private fun addContact() {
        val newRecord = requestNewRecord()
        records.add(newRecord)

        io.write("")
        io.write(Responses.RECORD_CREATED)
        io.write(Responses.PHONE_BOOK_CREATED)
    }

    context(RaiseInvalidInput)
    private fun requestNewRecord(): Record {
        io.write(Requests.NAME)
        val name = io.read().toNonEmptyOrNull() ?: raise(Error.InvalidInput("First name cannot be empty"))

        io.write(Requests.SURNAME)
        val surname = io.read().toNonEmptyOrNull() ?: raise(Error.InvalidInput("Surname cannot be empty"))

        io.write(Requests.PHONE_NUMBER)
        val phoneNumber = PhoneNumber(io.read()).onLeft { io.write(it.displayText.toString()) }.bind()

        return Record(name, surname, phoneNumber)
    }


    private object Requests {
        const val NAME = "Enter the name of the person:"
        const val SURNAME = "Enter the surname of the person:"
        const val PHONE_NUMBER = "Enter the number:"
    }

    private object Responses {
        const val RECORD_CREATED = "A record created!"
        const val PHONE_BOOK_CREATED = "A Phone Book with a single record created!"
    }
}
