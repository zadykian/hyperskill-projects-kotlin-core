package converter

object UnitName {
    private const val UNKNOWN_UNIT_NAME = "???"

    private val customSingularNames : Map<Unit<*>, String> = mapOf(
        Temperature.Celsius to "degree Celsius",
        Temperature.Fahrenheit to "degree Fahrenheit"
    )

    private val customPluralNames : Map<Unit<*>, String> = mapOf(
        Length.Inch to "inches",
        Length.Foot to "feet",
        Temperature.Celsius to "degrees Celsius",
        Temperature.Fahrenheit to "degrees Fahrenheit"
    )

    fun tryParse(unit: String): Unit<*>? = when (unit) {
        "mm", "millimeter", "millimeters" -> Length.Millimeter
        "cm", "centimeter", "centimeters" -> Length.Centimeter
        "m" , "meter"     , "meters"      -> Length.Meter
        "km", "kilometer" , "kilometers"  -> Length.Kilometer

        "in", "inch", "inches" -> Length.Inch
        "ft", "foot", "feet"   -> Length.Foot
        "yd", "yard", "yards"  -> Length.Yard
        "mi", "mile", "miles"  -> Length.Mile

        "g" , "gram"     , "grams"      -> Weight.Gram
        "mg", "milligram", "milligrams" -> Weight.Milligram
        "kg", "kilogram" , "kilograms"  -> Weight.Kilogram

        "lb", "pound", "pounds" -> Weight.Pound
        "oz", "ounce", "ounces" -> Weight.Ounce

        "degree celsius"   , "degrees celsius"   , "celsius"   , "dc", "c" -> Temperature.Celsius
        "degree fahrenheit", "degrees fahrenheit", "fahrenheit", "df", "f" -> Temperature.Fahrenheit
        "kelvin"           , "kelvins"                               , "k" -> Temperature.Kelvin

        else -> null
    }

    fun singular(unit : Unit<*>?): String =
        unit?.let { customSingularNames[it] ?: it.toString().lowercase() } ?: UNKNOWN_UNIT_NAME

    fun plural(unit : Unit<*>?) =
        unit?.let { customPluralNames[it] ?: (it.toString().lowercase() + 's') } ?: UNKNOWN_UNIT_NAME
}
