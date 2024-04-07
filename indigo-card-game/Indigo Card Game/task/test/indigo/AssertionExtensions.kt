package indigo

import assertk.Assert
import assertk.assertions.support.expected
import assertk.assertions.support.show

/**
 * Asserts that `values` contains at least one equivalent of `value`
 */
fun <T : Any?> Assert<T>.belongsTo(values: Iterable<T>) = given { value ->
    if (values.any { it == value }) return
    expected(":${show(values)} to contain:${show(value)}")
}
