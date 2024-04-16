package gitinternals

data class IO(val read: () -> String, val write: (String) -> Unit)
