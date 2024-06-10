package contacts.dynamic

import contacts.RaiseDynamicInvocationFailed

object DynamicStringBuilder {
    context(RaiseDynamicInvocationFailed)
    inline fun <reified T : Any> toString(obj: T) {
        TODO()
    }
}