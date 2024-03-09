package converter

data class Value<TUnit : Unit<TUnit>>(val number: Double, val unit: TUnit) {
    override fun toString(): String {
        val unitName = if (number == 1.0) UnitName.singular(unit) else UnitName.plural(unit)
        return "$number $unitName"
    }
}
