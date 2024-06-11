package contacts.dynamic

import arrow.core.Either
import arrow.core.raise.either
import contacts.Error
import contacts.RaiseDynamicInvocationFailed
import contacts.dynamic.DynamicObjectInvoker.getInvoker
import contacts.dynamic.annotations.DisplayName
import contacts.dynamic.annotations.DisplayOrder
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty

object DynamicObjectScanner {
    context(RaiseDynamicInvocationFailed)
    fun KAnnotatedElement.getDisplayName(): String {
        val displayName = annotations.filterIsInstance<DisplayName>().singleOrNull()?.name

        if (displayName != null) {
            return displayName
        }

        fun raise(): Nothing = raise(Error.DynamicInvocationFailed("Failed to get a display name for $this"))

        return when (this) {
            is KParameter -> name ?: raise()
            is KProperty<*> -> name
            is KClass<*> -> simpleName ?: raise()
            else -> raise()
        }
    }

    fun <T : Any> KClass<T>.getProperties(): Either<Error, List<PropOrParamMetadata>> = either {
        this@getProperties.getInvoker().bind().params
    }

    context(RaiseDynamicInvocationFailed)
    fun <T : Any> KClass<T>.getCases() =
        sealedSubclasses
            .map { Pair(it.getDisplayName(), it) }
            .sortedBy { it.second.annotations.filterIsInstance<DisplayOrder>().firstOrNull()?.order ?: 0 }
}
