package gitinternals

@Target(AnnotationTarget.FIELD)
annotation class CommandName(val name: String)

enum class Command {
    @CommandName("cat-file")
    CatFile,

    @CommandName("list-branches")
    ListBranches,

    @CommandName("log")
    Log,

    @CommandName("commit-tree")
    CommitTree;

    companion object {
        private val namesToCommands = entries.associateBy {
            it.declaringJavaClass.getField(it.name).getAnnotation(CommandName::class.java)?.name
                ?: throw IllegalArgumentException("CommandName is not defined from ${it.name}!")
        }

        fun getByNameOrNull(name: String): Command? = namesToCommands[name]
    }
}
