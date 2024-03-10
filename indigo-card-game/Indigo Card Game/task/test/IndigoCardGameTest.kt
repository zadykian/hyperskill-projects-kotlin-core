import org.hyperskill.hstest.dynamic.DynamicTest
import org.hyperskill.hstest.stage.StageTest
import org.hyperskill.hstest.testcase.CheckResult
import org.hyperskill.hstest.testing.TestedProgram

class CardGameTest : StageTest<Any>() {

    private val cardsLines = mutableListOf<String>()

    @DynamicTest(repeat = 3)
    fun printRanksSuitsCardsTest(): CheckResult {
        val main = TestedProgram()
        val outputString = main.start().trim()
        val lines = outputString.split('\n').map { it.trim() }.filter { it != "" }

        var ranksPrinted = -1
        var suitsPrinted = -1
        var cardsPrinted = -1
        for ((index, line) in lines.withIndex()) {
            if (isRanks(line)) ranksPrinted = index
            if (isSuits(line)) suitsPrinted = index
            if (isCards(line)) cardsPrinted = index
        }


        if (ranksPrinted == -1) return CheckResult(false, "Line with ranks isn't correct.")
        if (suitsPrinted == -1) return CheckResult(false, "Line with suits isn't correct.")
        if (cardsPrinted == -1) return CheckResult(false, "Line with all cards isn't correct.")

        val lineCards = lines[cardsPrinted].trim()
        cardsLines.add(lineCards)
        if (cardsLines.size != 1 && cardsLines.toSet().size == 1) {
            return CheckResult(false, "Line with all cards should be shuffled randomly" +
                    " each time the program is executed.")
        }
        return CheckResult.correct()
    }

}

fun isRanks(string: String): Boolean {
    val ranks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
    val outRanks = string.split(' ').map { it.trim() }.filter { it != "" }
    return (outRanks.containsAll(ranks) && ranks.size == outRanks.size)
}

fun isSuits(string: String): Boolean {
    val ranks = listOf("\u2666", "\u2665", "\u2660", "\u2663")
    val outRanks = string.split(' ').map { it.trim() }.filter { it != "" }
    return (outRanks.containsAll(ranks) && ranks.size == outRanks.size)
}

fun isCards(string: String): Boolean {
    val ranks = listOf(
            "A\u2660", "2\u2660", "3\u2660", "4\u2660", "5\u2660", "6\u2660", "7\u2660",
            "8\u2660", "9\u2660", "10\u2660", "J\u2660", "Q\u2660", "K\u2660",
            "A\u2665", "2\u2665", "3\u2665", "4\u2665", "5\u2665", "6\u2665", "7\u2665",
            "8\u2665", "9\u2665", "10\u2665", "J\u2665", "Q\u2665", "K\u2665",
            "A\u2666", "2\u2666", "3\u2666", "4\u2666", "5\u2666", "6\u2666", "7\u2666",
            "8\u2666", "9\u2666", "10\u2666", "J\u2666", "Q\u2666", "K\u2666",
            "A\u2663", "2\u2663", "3\u2663", "4\u2663", "5\u2663", "6\u2663", "7\u2663",
            "8\u2663", "9\u2663", "10\u2663", "J\u2663", "Q\u2663", "K\u2663")
    val outRanks = string.split(' ').map { it.trim() }.filter { it != "" }
    return (outRanks.containsAll(ranks) && ranks.size == outRanks.size)
}


