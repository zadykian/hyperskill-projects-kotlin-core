package indigo

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

data class ChooseCardTestCase(
    val topCardOnTable: Card?,
    val cardsInHand: List<Card>,
    val expectedChosenCard: Card?
)

class ComputerPlayerTest {
    @ParameterizedTest
    @MethodSource("provideTestCases")
    fun chooseCard(testcase: ChooseCardTestCase) {
        val computer = Computer()
        val chosenCard = computer.chooseCard(testcase.topCardOnTable, testcase.cardsInHand)
        Assertions.assertEquals(testcase.expectedChosenCard, chosenCard)
    }

    companion object {
        @JvmStatic
        fun provideTestCases() = listOf(
            ChooseCardTestCase(
                topCardOnTable = null,
                cardsInHand = listOf(Card(Suit.Clubs, Rank.Ace)),
                expectedChosenCard = Card(Suit.Clubs, Rank.Ace)
            ),
            ChooseCardTestCase(
                topCardOnTable = null,
                cardsInHand = listOf(Card(Suit.Clubs, Rank.Ace)),
                expectedChosenCard = Card(Suit.Clubs, Rank.Ace)
            )
        )
    }
}
