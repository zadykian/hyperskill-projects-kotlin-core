fun main() {
    html {
        body {

        }

        body {

        }
    }
}

class Body {

}

class Html {

    fun body(content: Body.() -> Unit) = Body()
}

fun html(body: Html.() -> Unit) = Html()

val isDivisible: Int.(Int) -> Boolean = { that -> this % that == 0 }
