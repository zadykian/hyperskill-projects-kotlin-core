package contacts

import arrow.core.Either
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.primaryConstructor

typealias PropertyName = String

object ObjectInitializer {
    context(RaiseAnyError)
    inline fun <reified T : Any> createNew(noinline valueReader: (PropertyName) -> String): T {
        val primaryCtorArgValues = getPrimaryCtorParams<T>()
            .bind()
            .map { (displayName, param) -> getParameterValue(valueReader, displayName, param) }
            .toTypedArray()

        return try {
            T::class.primaryConstructor!!.call(*primaryCtorArgValues)
        } catch (e: Exception) {
            raise(Error.FailedToCreateObject(e.localizedMessage))
        }
    }

    context(RaiseAnyError)
    @Suppress("UNCHECKED_CAST")
    fun getParameterValue(
        valueReader: (PropertyName) -> String,
        displayName: String,
        param: KParameter
    ): Any? {
        val companion = (param.type.classifier as KClass<*>).companionObject
            ?: raise(Error.FailedToCreateObject("Type ${param.type} is expected to have a companion object"))
        val ctor = companion.declaredMembers.firstOrNull { it.name == "invoke" }
            ?: raise(
                Error.FailedToCreateObject(
                    "Type ${param.type} is expected to have a function 'invoke' declared in companion object"
                )
            )

        val propertyValueCreated = Either.catch {
            val strValue = valueReader(displayName)
            ctor.call(companion.objectInstance, strValue) as Either<Error, *>
        }

        return when (propertyValueCreated) {
            is Either.Left -> raise(Error.FailedToCreateObject(propertyValueCreated.value.localizedMessage))
            is Either.Right -> propertyValueCreated.value.bind()
        }
    }

    inline fun <reified T : Any> getPrimaryCtorParams() =
        Either.catch {
            T::class
                .primaryConstructor!!
                .parameters
                .associateBy { param -> param.annotations.filterIsInstance<DisplayName>().single().name }
        }.mapLeft { Error.FailedToCreateObject(it.localizedMessage) }
}