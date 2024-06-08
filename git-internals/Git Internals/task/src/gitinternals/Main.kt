package gitinternals

fun main() {
    val io = IO(::readln, ::println)
    val application = Application(io)
    application.run()
}
