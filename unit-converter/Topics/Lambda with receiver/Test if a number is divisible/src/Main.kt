val isDivisible: Int.(Int) -> Boolean = { that -> this % that == 0 }

/* Do not change code below */
fun main() {
    val (a, b) = readln().split(' ').map { it.toInt() }
    println(a.isDivisible(b))
}
