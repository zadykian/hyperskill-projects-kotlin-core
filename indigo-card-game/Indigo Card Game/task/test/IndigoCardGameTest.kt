import org.hyperskill.hstest.dynamic.DynamicTest
import org.hyperskill.hstest.stage.StageTest
import org.hyperskill.hstest.testcase.CheckResult
import org.hyperskill.hstest.testing.TestedProgram

class ErrorData(val correct: Boolean, val errorMsg: String,
                val topCard: String = "", val cardsList: List<String> = emptyList())

class CardGameTest : StageTest<Any>() {

    @DynamicTest
    fun playFirstWrongInputTest(): CheckResult {
        val main = TestedProgram()
        var outputString = main.start().trim()
        var position = checkOutput(outputString.lowercase(), 0, "Indigo Card Game".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong game title.")
        position = checkOutput(outputString.lowercase(), position, "Play first?".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong play first prompt.")

        outputString = main.execute("Hello").trim()
        position = checkOutput(outputString.lowercase(), 0, "Play first?".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong output after wrong input after the play first question.")

        outputString = main.execute("me").trim()
        position = checkOutput(outputString.lowercase(), 0, "Play first?".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong output after wrong input after the play first question.")

        outputString = main.execute("0").trim()
        position = checkOutput(outputString.lowercase(), 0, "Play first?".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong output after wrong input after the play first question.")

        outputString = main.execute("play").trim()
        position = checkOutput(outputString.lowercase(), 0, "Play first?".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong output after wrong input after the play first question.")

        outputString = main.execute("yes").trim()
        var validOutput = checkInitial(outputString)
        if ( !validOutput.correct) return CheckResult(false, validOutput.errorMsg)
        val topCard = validOutput.topCard

        outputString = outputString.substringAfter(topCard).trim()
        validOutput = checkPlayerOutput(outputString, 6, 4, topCard)
        if ( !validOutput.correct) return CheckResult(false, validOutput.errorMsg)

        outputString = main.execute("exit").trim()
        position = checkOutput(outputString.lowercase(), 0, "Game Over".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong exit message.")

        if (!main.isFinished) return CheckResult(false, "Application hasn't exited after exit command.")

        return CheckResult.correct()
    }

    @DynamicTest
    fun playSecondWrongInputTest(): CheckResult {
        val main = TestedProgram()
        var outputString = main.start().trim()
        var position = checkOutput(outputString.lowercase(), 0, "Indigo Card Game".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong game title.")
        position = checkOutput(outputString.lowercase(), position, "Play first?".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong play first prompt.")

        outputString = main.execute("Hello").trim()
        position = checkOutput(outputString.lowercase(), 0, "Play first?".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong output after wrong input after the play first question.")

        outputString = main.execute("me").trim()
        position = checkOutput(outputString.lowercase(), 0, "Play first?".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong output after wrong input after the play first question.")

        outputString = main.execute("0").trim()
        position = checkOutput(outputString.lowercase(), 0, "Play first?".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong output after wrong input after the play first question.")

        outputString = main.execute("play").trim()
        position = checkOutput(outputString.lowercase(), 0, "Play first?".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong output after wrong input after the play first question.")

        outputString = main.execute("no").trim()
        var validOutput = checkInitial(outputString)
        if ( !validOutput.correct) return CheckResult(false, validOutput.errorMsg)
        var topCard = validOutput.topCard

        outputString = outputString.substringAfter(topCard).trim()
        validOutput = checkComputerOutput(outputString,4, topCard)
        if ( !validOutput.correct) return CheckResult(false, validOutput.errorMsg)
        topCard = validOutput.topCard

        outputString = outputString.substringAfter(topCard).trim()
        validOutput = checkPlayerOutput(outputString, 6, 5, topCard)
        if ( !validOutput.correct) return CheckResult(false, validOutput.errorMsg)

        outputString = main.execute("exit").trim()
        position = checkOutput(outputString.lowercase(), 0, "Game Over".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong exit message.")

        if (!main.isFinished) return CheckResult(false, "Application hasn't exited after exit command.")

        return CheckResult.correct()
    }

    @DynamicTest
    fun wrongCardToPlayTest(): CheckResult {
        val main = TestedProgram()
        var outputString = main.start().trim()
        var position = checkOutput(outputString.lowercase(), 0, "Indigo Card Game".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong game title.")
        position = checkOutput(outputString.lowercase(), position, "Play first?".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong play first prompt.")

        outputString = main.execute("yes").trim()
        var validOutput = checkInitial(outputString)
        if ( !validOutput.correct) return CheckResult(false, validOutput.errorMsg)
        val topCard = validOutput.topCard

        outputString = outputString.substringAfter(topCard).trim()
        validOutput = checkPlayerOutput(outputString, 6, 4, topCard)
        if ( !validOutput.correct) return CheckResult(false, validOutput.errorMsg)

        outputString = main.execute("0").trim()
        position = checkOutput(outputString.lowercase(), 0, "Choose a card to play (1-6):".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong output, after input an invalid card number.")

        outputString = main.execute("7").trim()
        position = checkOutput(outputString.lowercase(), 0, "Choose a card to play (1-6):".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong output, after input an invalid card number.")

        outputString = main.execute("One").trim()
        position = checkOutput(outputString.lowercase(), 0, "Choose a card to play (1-6):".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong output, after input an invalid card number.")


        outputString = main.execute("exit").trim()
        position = checkOutput(outputString.lowercase(), 0, "Game Over".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong exit message.")

        if (!main.isFinished) return CheckResult(false, "Application hasn't exited after exit command.")

        return CheckResult.correct()
    }

    @DynamicTest
    fun playFirstNormalExeTest1(): CheckResult {
        val deck = mutableListOf<String>()
        val cardsInHand = mutableListOf<String>()

        val main = TestedProgram()
        var outputString = main.start().trim()
        var position = checkOutput(outputString.lowercase(), 0, "Indigo Card Game".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong game title.")
        position = checkOutput(outputString.lowercase(), position, "Play first?".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong play first prompt.")

        outputString = main.execute("yes").trim()
        var validOutput = checkInitial(outputString)
        if ( !validOutput.correct) return CheckResult(false, validOutput.errorMsg)
        deck.addAll(validOutput.cardsList)
        var topCard = validOutput.topCard

        outputString = outputString.substringAfter(topCard).trim()
        var numOfCardsOnTable = 4
        repeat(4) {
            for (numOfCards in 6 downTo 1) {
                validOutput = checkPlayerOutput(outputString, numOfCards, numOfCardsOnTable, topCard)
                if ( !validOutput.correct) return CheckResult(false, validOutput.errorMsg)
                if (numOfCards == 6) {
                    cardsInHand.clear()
                    cardsInHand.addAll(validOutput.cardsList)
                    for (card in cardsInHand)
                        if ( deck.contains(card) )
                            return CheckResult(false, "Some cards in hand have already passed on table (Duplicates).")
                    deck.addAll(cardsInHand)
                } else {
                    if ( !cardsInHand.containsAll(validOutput.cardsList) )
                        return CheckResult(false, "Cards in hand have changed since the last card was played.")
                }
                topCard = validOutput.cardsList.first()
                numOfCardsOnTable++
                cardsInHand.remove(topCard)
                outputString = main.execute("1").trim()

                validOutput = checkComputerOutput(outputString, numOfCardsOnTable, topCard)
                if (!validOutput.correct) return CheckResult(false, validOutput.errorMsg)
                topCard = validOutput.topCard
                if ( deck.contains(topCard) )
                    return CheckResult(false, "Computer played card is a duplicate.")
                deck.add(topCard)
                numOfCardsOnTable++
                outputString = outputString.substringAfter(topCard).trim()
            }
        }

        position = checkOutput(outputString.lowercase(), 0, "52 cards on the table, and the top card is $topCard".lowercase(), "Game Over".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong exit message.")
        if (!main.isFinished) return CheckResult(false, "Application hasn't exited after exit command.")

        return CheckResult.correct()
    }

    @DynamicTest
    fun playFirstNormalExeTest2(): CheckResult {
        val deck = mutableListOf<String>()
        val cardsInHand = mutableListOf<String>()

        val main = TestedProgram()
        var outputString = main.start().trim()
        var position = checkOutput(outputString.lowercase(), 0, "Indigo Card Game".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong game title.")
        position = checkOutput(outputString.lowercase(), position, "Play first?".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong play first prompt.")

        outputString = main.execute("yes").trim()
        var validOutput = checkInitial(outputString)
        if ( !validOutput.correct) return CheckResult(false, validOutput.errorMsg)
        deck.addAll(validOutput.cardsList)
        var topCard = validOutput.topCard

        outputString = outputString.substringAfter(topCard).trim()
        var numOfCardsOnTable = 4
        repeat(4) {
            for (numOfCards in 6 downTo 1) {
                validOutput = checkPlayerOutput(outputString, numOfCards, numOfCardsOnTable, topCard)
                if ( !validOutput.correct) return CheckResult(false, validOutput.errorMsg)
                if (numOfCards == 6) {
                    cardsInHand.clear()
                    cardsInHand.addAll(validOutput.cardsList)
                    for (card in cardsInHand)
                        if ( deck.contains(card) )
                            return CheckResult(false, "Some cards in hand have already passed on table (Duplicates).")
                    deck.addAll(cardsInHand)
                } else {
                    if ( !cardsInHand.containsAll(validOutput.cardsList) )
                        return CheckResult(false, "Cards in hand have changed since the last card was played.")
                }
                topCard = validOutput.cardsList.last()
                numOfCardsOnTable++
                cardsInHand.remove(topCard)
                outputString = main.execute("$numOfCards").trim()

                validOutput = checkComputerOutput(outputString, numOfCardsOnTable, topCard)
                if (!validOutput.correct) return CheckResult(false, validOutput.errorMsg)
                topCard = validOutput.topCard
                if ( deck.contains(topCard) )
                    return CheckResult(false, "Computer played card is a duplicate.")
                deck.add(topCard)
                numOfCardsOnTable++
                outputString = outputString.substringAfter(topCard).trim()
            }
        }

        position = checkOutput(outputString.lowercase(), 0, "52 cards on the table, and the top card is $topCard".lowercase(), "Game Over".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong exit message.")
        if (!main.isFinished) return CheckResult(false, "Application hasn't exited after exit command.")

        return CheckResult.correct()
    }

    @DynamicTest
    fun playSecondNormalExeTest1(): CheckResult {
        val deck = mutableListOf<String>()
        val cardsInHand = mutableListOf<String>()

        val main = TestedProgram()
        var outputString = main.start().trim()
        var position = checkOutput(outputString.lowercase(), 0, "Indigo Card Game".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong game title.")
        position = checkOutput(outputString.lowercase(), position, "Play first?".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong play first prompt.")

        outputString = main.execute("no").trim()
        var validOutput = checkInitial(outputString)
        if ( !validOutput.correct) return CheckResult(false, validOutput.errorMsg)
        deck.addAll(validOutput.cardsList)
        var topCard = validOutput.topCard

        outputString = outputString.substringAfter(topCard).trim()
        var numOfCardsOnTable = 4
        repeat(4) {
            for (numOfCards in 6 downTo 1) {
                validOutput = checkComputerOutput(outputString, numOfCardsOnTable, topCard)
                if (!validOutput.correct) return CheckResult(false, validOutput.errorMsg)
                topCard = validOutput.topCard
                if ( deck.contains(topCard) )
                    return CheckResult(false, "Computer played card is a duplicate.")
                deck.add(topCard)
                numOfCardsOnTable++
                outputString = outputString.substringAfter(topCard).trim()

                validOutput = checkPlayerOutput(outputString, numOfCards, numOfCardsOnTable, topCard)
                if ( !validOutput.correct) return CheckResult(false, validOutput.errorMsg)
                if (numOfCards == 6) {
                    cardsInHand.clear()
                    cardsInHand.addAll(validOutput.cardsList)
                    for (card in cardsInHand)
                        if ( deck.contains(card) )
                            return CheckResult(false, "Some cards in hand have already passed on table (Duplicates).")
                    deck.addAll(cardsInHand)
                } else {
                    if ( !cardsInHand.containsAll(validOutput.cardsList) )
                        return CheckResult(false, "Cards in hand have changed since the last card was played.")
                }
                topCard = validOutput.cardsList.first()
                numOfCardsOnTable++
                cardsInHand.remove(topCard)
                outputString = main.execute("1").trim()
            }
        }

        position = checkOutput(outputString.lowercase(), 0, "52 cards on the table, and the top card is $topCard".lowercase(), "Game Over".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong exit message.")
        if (!main.isFinished) return CheckResult(false, "Application hasn't exited after exit command.")

        return CheckResult.correct()
    }

    @DynamicTest
    fun playSecondNormalExeTest2(): CheckResult {
        val deck = mutableListOf<String>()
        val cardsInHand = mutableListOf<String>()

        val main = TestedProgram()
        var outputString = main.start().trim()
        var position = checkOutput(outputString.lowercase(), 0, "Indigo Card Game".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong game title.")
        position = checkOutput(outputString.lowercase(), position, "Play first?".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong play first prompt.")

        outputString = main.execute("no").trim()
        var validOutput = checkInitial(outputString)
        if ( !validOutput.correct) return CheckResult(false, validOutput.errorMsg)
        deck.addAll(validOutput.cardsList)
        var topCard = validOutput.topCard

        outputString = outputString.substringAfter(topCard).trim()
        var numOfCardsOnTable = 4
        repeat(4) {
            for (numOfCards in 6 downTo 1) {
                validOutput = checkComputerOutput(outputString, numOfCardsOnTable, topCard)
                if (!validOutput.correct) return CheckResult(false, validOutput.errorMsg)
                topCard = validOutput.topCard
                if ( deck.contains(topCard) )
                    return CheckResult(false, "Computer played card is a duplicate.")
                deck.add(topCard)
                numOfCardsOnTable++
                outputString = outputString.substringAfter(topCard).trim()

                validOutput = checkPlayerOutput(outputString, numOfCards, numOfCardsOnTable, topCard)
                if ( !validOutput.correct) return CheckResult(false, validOutput.errorMsg)
                if (numOfCards == 6) {
                    cardsInHand.clear()
                    cardsInHand.addAll(validOutput.cardsList)
                    for (card in cardsInHand)
                        if ( deck.contains(card) )
                            return CheckResult(false, "Some cards in hand have already passed on table (Duplicates).")
                    deck.addAll(cardsInHand)
                } else {
                    if ( !cardsInHand.containsAll(validOutput.cardsList) )
                        return CheckResult(false, "Cards in hand have changed since the last card was played.")
                }
                topCard = validOutput.cardsList.last()
                numOfCardsOnTable++
                cardsInHand.remove(topCard)
                outputString = main.execute("$numOfCards").trim()
            }
        }

        position = checkOutput(outputString.lowercase(), 0, "52 cards on the table, and the top card is $topCard".lowercase(), "Game Over".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong exit message.")
        if (!main.isFinished) return CheckResult(false, "Application hasn't exited after exit command.")

        return CheckResult.correct()
    }

}

fun checkOutput(outputString: String, searchPos: Int, vararg checkStr: String): Int {
    var searchPosition = searchPos
    for (str in checkStr) {
        val findPosition = outputString.indexOf(str, searchPosition)
        if (findPosition == -1) return -1
        if ( outputString.substring(searchPosition until findPosition).isNotBlank() ) return -1
        searchPosition = findPosition + str.length
    }
    return searchPosition
}

fun checkOIfValidCards2(cardsStr: String, numOfCards: Int): Boolean {
    val cards = cardsStr.split(" ")
    val cardRegex = "(A|[2-9]|10|J|Q|K)(\\u2666|\\u2665|\\u2660|\\u2663)".toRegex()
    for (card in cards) if (!card.matches(cardRegex)) {
        return false
    }
    return cards.size == numOfCards
}

fun checkIfUniqueCards(outputString: String): Boolean {
    val lines = outputString.lines()
    val cards = lines.first().trim().split(" ")
    return cards.distinct().size == cards.size
}

fun checkOIfValidCardsInHand(cardsStr: String, numOfCards: Int): Boolean {
    val cards = cardsStr.split(" ")
    val cardRegex = "([1-6])\\)(A|[2-9]|10|J|Q|K)(\\u2666|\\u2665|\\u2660|\\u2663)".toRegex()
    for (card in cards) if (!card.matches(cardRegex)) {
        return false
    }
    return cards.size == numOfCards
}

fun checkInitial(output: String): ErrorData {
    val position = checkOutput(output.lowercase(), 0, "Initial cards on the table:".lowercase())
    if ( position  == -1 ) return ErrorData(false, "Wrong Initial cards message.")
    val cardsStr = output.substring(position).lines().map { it.trim() }.first { it != "" }
    if (cardsStr.isEmpty()) return ErrorData(false, "No initial cards are printed.")
    if ( !checkOIfValidCards2(cardsStr, 4) ) return ErrorData(false, "Invalid initial cards.")
    if ( !checkIfUniqueCards(cardsStr) ) return ErrorData(false, "Initial cards contain duplicate cards.")
    val cardsOnTable = cardsStr.trim().split(" ")
    val topCard = cardsOnTable.last()
    return ErrorData(true, "", topCard, cardsOnTable)
}

fun checkPlayerOutput(output: String, numOfCards: Int, numOfCardsOnTable : Int, topCard: String): ErrorData {
    var position = checkOutput(output.lowercase(), 0, "$numOfCardsOnTable cards on the table, and the top card is $topCard".lowercase())
    if ( position  == -1 ) return ErrorData(false, "Player turn: Wrong message for number of cards and top card.")
    position = checkOutput(output.lowercase(), position, "Cards in hand:".lowercase())
    if ( position  == -1 ) return ErrorData(false, "Wrong cards in hand message.")
    val cardsInHand = output.substring(position).lines().map { it.trim() }.firstOrNull() { it != "" }
        ?: return ErrorData(false, "Wrong cards in hand message.")
    if (!checkOIfValidCardsInHand(cardsInHand, numOfCards)) return ErrorData(false, "Wrong cards in hand message.")
    val listCardsInHand = cardsInHand.split(" ").map { it.substring(2) }
    var strCardsInHand = ""
    for (card in listCardsInHand) strCardsInHand += "$card "
    if ( !checkIfUniqueCards(strCardsInHand) ) return ErrorData(false, "Player's cards in hand contain duplicate cards.")
    val lastCard = listCardsInHand.last()
    position = output.indexOf(lastCard, position)
    position = checkOutput(output.lowercase(), position + lastCard.length, "Choose a card to play (1-$numOfCards):".lowercase())
    if ( position  == -1 ) return ErrorData(false, "Wrong prompt to choose a card.")
    return ErrorData(true, "", cardsList = listCardsInHand)
}

fun checkComputerOutput(output: String, numOfCardsOnTable : Int, topCard: String): ErrorData {
    var position = checkOutput(output.lowercase(), 0, "$numOfCardsOnTable cards on the table, and the top card is $topCard".lowercase())
    if ( position  == -1 ) return ErrorData(false, "Computer turn: Wrong output for number of cards and the top card.")
    position = checkOutput(output.lowercase(), position, "Computer plays".lowercase())
    if ( position  == -1 ) return ErrorData(false, "Wrong computer plays a card message.")
    val endIndex = output.indexOf("\n", position)
    if (endIndex < 0) return ErrorData(false, "Wrong output. Some lines are missing")
    val card = output.substring(position, endIndex).trim()
    if (!checkOIfValidCards2(card, 1)) return ErrorData(false, "Wrong computer plays invalid card.")
    return ErrorData(true, "", card)
}


