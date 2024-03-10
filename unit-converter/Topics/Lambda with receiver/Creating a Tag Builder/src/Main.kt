/* Do not change code below */
fun builderString(
    builder: StringBuilder.(String) -> Unit
): String {
    val stringBuilder = StringBuilder()
    stringBuilder.builder("")
    return stringBuilder.toString()
}

fun main() {
    val a = readln()
    val res = builderString {
        append("<tag>")
        append(a)
        append("</tag>")
    }
    println(res)
}
