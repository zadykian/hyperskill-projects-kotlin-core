package converter

private fun printFinal(output: String) {
    println(output)
    println()
}

private tailrec fun askUserForNextConversion() {
    print(Messages.GREETING)
    val inputString = readln().lowercase()

    if (inputString == Messages.EXIT) {
        return
    }

    val (userInput, failureMessage) = UserInputParser.tryParse(inputString)
    if (userInput == null) {
        printFinal(failureMessage!!)
    }
    else {
        fun <TUnit: Unit<TUnit>> convert(input: ConversionInput<TUnit>)
            = input.originalValue.unit.convert(input.originalValue, input.targetUnit)

        val newValue = convert(userInput)
        printFinal("${userInput.originalValue} is $newValue")
    }

    askUserForNextConversion()
}

fun main() = askUserForNextConversion()
