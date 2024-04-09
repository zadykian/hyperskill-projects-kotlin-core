package gitinternals

interface IO {
    fun read(): String
    fun write(value: String)
}