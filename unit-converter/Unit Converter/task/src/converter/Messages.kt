package converter

internal object Messages {
    const val GREETING = "Enter what you want to convert (or exit): "
    const val PARSE_ERROR = "Parse error"
    const val EXIT = "exit"

    fun invalidUnitPair(source: Unit<*>?, target: Unit<*>?) =
        "Conversion from ${UnitName.plural(source)} to ${UnitName.plural(target)} is impossible"

    fun invalidInputValue(unitTypeName: String) = "$unitTypeName shouldn't be negative"
}
