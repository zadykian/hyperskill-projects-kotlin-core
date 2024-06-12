package contacts

import contacts.storage.InMemoryPhoneBook

fun main() {
    val phoneBook = InMemoryPhoneBook()
    val io = IO(::readln, ::println)
    val application = Application(phoneBook, io)
    application.run()
}
