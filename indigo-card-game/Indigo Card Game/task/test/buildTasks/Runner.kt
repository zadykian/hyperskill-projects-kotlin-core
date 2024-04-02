@file:JvmName("Runner")

package buildTasks

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import indigo.Card
import indigo.Deck
import indigo.Rank
import indigo.Suit

@Suppress("UNUSED_PARAMETER")
fun main(args: Array<String>) = generateCardsSource()

fun generateCardsSource() {
    val properties = Deck
        .allCards
        .asSequence()
        .map { declareProperty(it) }
        .asIterable()

    val type = TypeSpec
        .classBuilder(name = "Cards")
        .addProperties(properties)
        .build()

    val file = FileSpec
        .builder(packageName = "indigo.generated", fileName = "Cards.generated.kt")
        .addImport(packageName = "indigo", Suit::class.simpleName!!, Rank::class.simpleName!!)
        .addType(type)
        .build()

    file.writeTo(System.out)
}

fun declareProperty(card: Card) =
    PropertySpec
        .builder(name = "${card.rank.toString().lowercase()}Of${card.suit}", Card::class)
        .initializer(
            CodeBlock.of(
                "${Card::class.simpleName}(${Suit::class.simpleName}.${card.suit}, ${Rank::class.simpleName}.${card.rank})"
            )
        )
        .build()