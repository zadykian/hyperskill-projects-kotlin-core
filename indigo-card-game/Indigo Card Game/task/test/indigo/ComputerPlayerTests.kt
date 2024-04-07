package indigo

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isIn
import assertk.assertions.isNull
import indigo.generated.cards.*
import org.junit.jupiter.api.Test
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
    private val computer = Computer()

    @Test
    fun `Return null on empty hand`() {
        val chosenCard = computer.chooseCard(topCardOnTable = Deck.allCards.random(), cardsInHand = emptyList())
        assertThat(chosenCard).isNull()
    }

    @ParameterizedTest
    @MethodSource("testCases")
    fun `Choose card from hand`(testcase: ChooseCardTestCase) {
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
            ChooseCardTestCase(
                name = "Empty table; One card in hand",
                topCardOnTable = null,
                cardsInHand = setOf(aceOfClubs),
                expected = Expects.Specific(aceOfClubs)
            ),
            ChooseCardTestCase(
                name = "Empty table; Cards with same suit in hand",
                topCardOnTable = null,
                cardsInHand = setOf(twoOfClubs, kingOfDiamonds, fourOfHearts, queenOfSpades, jackOfHearts),
                expected = Expects.AnyOf(fiveOfHearts, jackOfHearts)
            ),
            ChooseCardTestCase(
                name = "Empty table; Cards with same rank in hand",
                topCardOnTable = null,
                cardsInHand = setOf(aceOfDiamonds, twoOfClubs, twoOfHearts, queenOfSpades),
                expected = Expects.AnyOf(twoOfClubs, twoOfHearts)
            ),
            ChooseCardTestCase(
                name = "Empty table; No cards in hand with the same suit or rank",
                topCardOnTable = null,
                cardsInHand = setOf(nineOfHearts, eightOfClubs, aceOfSpades, threeOfDiamonds),
                expected = Expects.AnyOf(nineOfHearts, eightOfClubs, aceOfSpades, threeOfDiamonds)
            ),
            ChooseCardTestCase(
                name = "Non-empty table; No candidate cards; One card in hand",
                topCardOnTable = kingOfDiamonds,
                cardsInHand = setOf(aceOfClubs),
                expected = Expects.Specific(aceOfClubs)
            ),
            ChooseCardTestCase(
                name = "Non-empty table; No candidate cards; Cards with same suit in hand",
                topCardOnTable = aceOfDiamonds,
                cardsInHand = setOf(sixOfClubs, queenOfHearts, eightOfClubs, jackOfSpades, sevenOfClubs),
                expected = Expects.AnyOf(sixOfClubs, eightOfClubs, sevenOfClubs)
            ),
            ChooseCardTestCase(
                name = "Non-empty table; No candidate cards; Cards with same rank in hand",
                topCardOnTable = aceOfDiamonds,
                cardsInHand = setOf(queenOfHearts, jackOfSpades, jackOfClubs),
                expected = Expects.AnyOf(jackOfSpades, jackOfClubs)
            ),
            ChooseCardTestCase(
                name = "Non-empty table; No candidate cards; No cards in hand with the same suit or rank",
                topCardOnTable = aceOfDiamonds,
                cardsInHand = setOf(jackOfSpades, queenOfHearts, kingOfClubs),
                expected = Expects.AnyOf(jackOfSpades, queenOfHearts, kingOfClubs)
            ),
        )
    }
}
