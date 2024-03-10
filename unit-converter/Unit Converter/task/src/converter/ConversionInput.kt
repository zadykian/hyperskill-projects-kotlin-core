package converter

data class ConversionInput<TUnit : Unit<TUnit>>(val originalValue: Value<TUnit>, val targetUnit: TUnit)

object UserInputParser {

    private fun getInputTokens(input: String) : Result<Triple<String, String, String>> {
        val floatPattern = """[+-]?\d+(\.\d+)?"""
        val unitPattern = """(degrees?\s+)?\w+"""
        val pattern = """^\s*(?<number>$floatPattern)\s+(?<source>$unitPattern)\s+\w+\s+(?<target>$unitPattern)\s*$""".toRegex()

        val matchResult = pattern.matchEntire(input)
            ?: return Result.failure(IllegalArgumentException(Messages.PARSE_ERROR))

        val triple = Triple(
            matchResult.groups["number"]!!.value,
            matchResult.groups["source"]!!.value,
            matchResult.groups["target"]!!.value,
        )

        return Result.success(triple)
    }

    fun tryParse(input: String) : Pair<ConversionInput<*>?, String?> {
        fun success(input: ConversionInput<*>) = Pair(input, null)
        fun failure(message: String) = Pair(null, message)

        val inputResult = getInputTokens(input).getOrNull() ?: return failure(Messages.PARSE_ERROR)

        val inputNumber = inputResult.first.toDoubleOrNull() ?: return failure(Messages.PARSE_ERROR)

        val sourceUnit = UnitName.tryParse(inputResult.second)
        val targetUnit = UnitName.tryParse(inputResult.third)

        if (sourceUnit == null
            || targetUnit == null
            || sourceUnit::class != targetUnit::class) {
            return failure(Messages.invalidUnitPair(sourceUnit, targetUnit))
        }

        if (!sourceUnit.validRange.contains(inputNumber)) {
            return failure(Messages.invalidInputValue(sourceUnit::class.simpleName!!))
        }

        val conversionInput = tryCreateInput(inputNumber, sourceUnit, targetUnit)

        return if (conversionInput == null) failure(Messages.invalidUnitPair(sourceUnit, targetUnit))
        else success(conversionInput)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <TUnit : Unit<TUnit>> tryCreateInput(number: Double, sourceUnit: Any, targetUnit: Any)
        : ConversionInput<TUnit>? {
        return try {
            ConversionInput(
                originalValue = Value(number, sourceUnit as TUnit),
                targetUnit = targetUnit as TUnit
            )
        } catch (exception: ClassCastException) {
            null
        }
    }
}
