/* Do not change code below */
data class Account(
    val id: Int = 1,
    val amount: Int = 0,
    val result: Int = 0,
    val status: String = "START"
)

fun calculateOperation(
    init: Account,
    func: Account.() -> Account
): Account {
    val account = init.copy(amount = 50)
    return account.func()
}

/* Do not change code below */
fun main() {
    val (INC, CTE) = readLine()!!.split(' ').map { it.toInt() }

    val res = calculateOperation(Account()) {
        val resultValue = amount * INC * CTE
        copy(result = resultValue, status = "END")
    }
    println(res)
}
