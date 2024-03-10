package converter

data class Range<T>(val minValue: T, val maxValue: T) where T : Comparable<T> {
    init {
        if (minValue > maxValue) {
            throw IllegalArgumentException("'${::minValue.name}' cannon be greater then '${::maxValue.name}'!")
        }
    }

    fun contains(value: T) = value in minValue..maxValue

    companion object {
        val nonNegativeNumbers = Range(0.0, Double.MAX_VALUE)
    }
}