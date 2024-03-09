package converter

interface Unit<TSelf> where TSelf : Unit<TSelf> {
    val validRange: Range<Double>
    fun convert(originalValue: Value<TSelf>, targetUnit: TSelf) : Value<TSelf>
}

private interface HasCoefficient {
    val coefficient: Double
}

private fun <TUnit> convertWithCoefficient(
    originalValue: Value<TUnit>,
    targetUnit: TUnit
) : Value<TUnit>
    where
        TUnit : Unit<TUnit>,
        TUnit : HasCoefficient {
    if (originalValue.unit == targetUnit) {
        return originalValue
    }
    val oldNormalized = originalValue.number * originalValue.unit.coefficient
    val newNumber = oldNormalized / targetUnit.coefficient
    return Value(newNumber, targetUnit)
}

enum class Length(
    override val coefficient: Double,
    override val validRange: Range<Double> = Range.nonNegativeNumbers,
) : Unit<Length>, HasCoefficient {

    Millimeter(0.001),
    Centimeter(0.01),
    Meter(1.0),
    Kilometer(1000.0),

    Inch(0.0254),
    Foot(0.3048),
    Yard(0.9144),
    Mile(1609.35);

    override fun convert(originalValue: Value<Length>, targetUnit: Length): Value<Length> =
        convertWithCoefficient(originalValue, targetUnit)
}

enum class Weight(
    override val coefficient: Double,
    override val validRange: Range<Double> = Range.nonNegativeNumbers,
) : Unit<Weight>, HasCoefficient {

    Gram(1.0),
    Milligram(0.001),
    Kilogram(1000.0),

    Ounce(28.3495),
    Pound(453.592);

    override fun convert(originalValue: Value<Weight>, targetUnit: Weight): Value<Weight> =
        convertWithCoefficient(originalValue, targetUnit)
}

typealias TemperatureConversions = Map<Pair<Temperature, Temperature>, (Double) -> Double>

enum class Temperature(
    override val validRange: Range<Double>
) : Unit<Temperature> {

    Celsius(validRange = Range(-273.15, Double.MAX_VALUE)),
    Fahrenheit(validRange = Range(-459.67, Double.MAX_VALUE)),
    Kelvin(validRange = Range.nonNegativeNumbers);

    private val conversions: TemperatureConversions by lazy { createConversions() }

    private fun createConversions() : TemperatureConversions = mapOf(
        Pair(Celsius, Fahrenheit) to { (it * 9.0/5.0) + 32.0 },
        Pair(Fahrenheit, Celsius) to { (it - 32.0) * 5.0/9.0 },

        Pair(Fahrenheit, Kelvin) to { (it + 459.67) * 5.0/9.0 },
        Pair(Kelvin, Fahrenheit) to { (it * 9.0/5.0) - 459.67 },

        Pair(Kelvin, Celsius) to { it - 273.15 },
        Pair(Celsius, Kelvin) to { it + 273.15 },
    )

    override fun convert(originalValue: Value<Temperature>, targetUnit: Temperature): Value<Temperature> {
        if (originalValue.unit == targetUnit) {
            return originalValue
        }

        val pair = Pair(originalValue.unit, targetUnit)
        val newValue = conversions[pair]?.invoke(originalValue.number)
            ?: throw Exception("Conversion func for pair $pair is not defined!")

        return Value(newValue, targetUnit)
    }
}
