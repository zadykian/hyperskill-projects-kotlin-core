package indigo

abstract class UserAction private constructor() {
    object Reset : UserAction()

    object Shuffle : UserAction()

    class GetCards(val numberOfCards: Int) : UserAction()

    object Exit : UserAction()
}