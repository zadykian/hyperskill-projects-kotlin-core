package indigo

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isIn
import indigo.generated.cards.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

sealed class Expects private constructor() {
    class Specific(val card: Card) : Expects()
    class AnyOf(vararg cards: Card) : Expects() {
        val cards = cards.toSet()

        init {
            require(cards.isNotEmpty())
        }
    }
}

data class ChooseCardTestCase(
    val name: String,
    val topCardOnTable: Card?,
    val cardsInHand: Set<Card>,
    val expected: Expects
) {
    override fun toString() = name
}

class ComputerPlayerTests {
    @ParameterizedTest
    @MethodSource("testCases")
    fun chooseCard(testcase: ChooseCardTestCase) {
        val computer = Computer()
        val chosenCard = computer.chooseCard(testcase.topCardOnTable, testcase.cardsInHand.toList())

        assertThat(chosenCard).let {
            when (testcase.expected) {
                is Expects.Specific -> it.isEqualTo(testcase.expected.card)
                is Expects.AnyOf -> it.isIn(testcase.expected.cards)
            }
        }
    }

    companion object {
        @JvmStatic
        fun testCases() = listOf(
            ChooseCardTestCase(
                name = "One card in hand; Empty table",
                topCardOnTable = null,
                cardsInHand = setOf(aceOfClubs),
                expected = Expects.Specific(aceOfClubs)
            ),
            ChooseCardTestCase(
                name = "One card in hand; Non-empty table",
                topCardOnTable = Deck.allCards.random(),
                cardsInHand = setOf(fiveOfHearts),
                expected = Expects.Specific(fiveOfHearts)
            ),
            ChooseCardTestCase(
                name = "One candidate card",
                topCardOnTable = fiveOfDiamonds,
                cardsInHand = setOf(twoOfClubs, aceOfHearts, fiveOfDiamonds, kingOfSpades),
                expected = Expects.Specific(fiveOfDiamonds)
            ),
        )
    }
}
