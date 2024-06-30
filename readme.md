# Kotlin Core

![logo](.common/jetbrains-academy-logo.png)

This repository houses several projects developed during `Kotlin Core` course provided by `JetBrains Academy`

The projects are developed using `Kotlin 2.0` and several additional packages:

1. [kotlin-reflect](https://github.com/JetBrains/kotlin/tree/037b3697ed635a52c283da7b2bf6ecd0961ce8f4/libraries/stdlib/jvm/src/kotlin/reflect) for some metadata manipulation
2. [arrow](https://github.com/arrow-kt/arrow) in combination with context parameters
3. [kotlinpoet](https://github.com/square/kotlinpoet) to generate test case data in [indigo-card-game](indigo-card-game/Indigo%20Card%20Game/task/test/codeGeneration)
4. [junit-jupiter](https://github.com/junit-team/junit5) and [assertk](https://github.com/willowtreeapps/assertk) for unit testing

## Projects

All these projects are essentially console applications which communicate with the user in a request-response manner

List is ordered by project completion date

### ⚖️ [unit-converter](unit-converter/Unit%20Converter/task/src/converter)

A simple app which parsers and converts different types of unit (length, weight and temperature).
The core conversion logic is located in [Unit.kt](unit-converter/Unit%20Converter/task/src/converter/Unit.kt)

### 🧮 [smart-calculator](smart-calculator/Smart%20Calculator%20(Kotlin)/task/src/calculator)

An arithmetic calculator which supports main operators `[+, -, *, /, ^]`, calculations in parentheses and variable assignment.

Calculation is based on the [Polish Postfix Notation](https://en.wikipedia.org/wiki/Reverse_Polish_notation)
and implemented in [Calculator.kt](smart-calculator/Smart%20Calculator%20(Kotlin)/task/src/calculator/Calculator.kt)

Expression tokenization and parsing is located in package `calculator.parser`

### 🃏 [indigo-card-game](indigo-card-game/Indigo%20Card%20Game/task/src/indigo)

A text-only card game with several neat features:

* [2-8] players supported;
* Two kinds of [Player](indigo-card-game/Indigo%20Card%20Game/task/src/indigo/Player.kt): `User` and `Computer`;
* Each game can be played by any number of users/computers (as long as the total number of players is correct);
* Fully immutable state with event-based logic: [GameEvent.kt](indigo-card-game/Indigo%20Card%20Game/task/src/indigo/GameEvent.kt)

### 🛠 [git-internals](git-internals/Git%20Internals/task/src/gitinternals)

CLI tool which provides several commands to observe git repository content:

* `cat-file` - display git object's (commit, tree or blob) content with the specified hash
* `list-branches` - list all repository branches and specify which is the current one
* `log` - show a full commit history for a specified branch
* `commit-tree` - show a full git file system tree of a commit with the specified hash 

**This one is my favorite because it required a lot of reverse engineering,
and it helped me to get a better understanding of git internal structure**


### 📠 [contacts](contacts/Contacts%20(Kotlin)/task/src)

A phone book application with several contact types supported.

Implementation is heavily based on `Arrow` library - `Either<E, R>`, `Ior<E, R>`, `Raise<Error>` types and opted-in Kotlin context parameters.

Also in this project I used `Either`-returning constructor technique (like [here](contacts/Contacts%20(Kotlin)/task/src/domain/PhoneNumber.kt)),
which I learned about from a great book by Alejandro Serrano Mena: [FP Ideas for the Curious Kotliner](https://leanpub.com/fp-ideas-kotlin)
