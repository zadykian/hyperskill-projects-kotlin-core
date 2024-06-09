package contacts

import arrow.core.Either
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.primaryConstructor

typealias PropertyName = String

object ObjectInitializer {
    context(RaiseAnyError)
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any> createNew(crossinline valueReader: (PropertyName) -> String): T {
        val privateCtorParamValues = getPrimaryCtorParams<T>()
            .bind()
            .map { (displayName, param) ->
                val propertyValueCreated = Either.catch {
                    val strValue = valueReader(displayName)
                    val ctor = param.type::class.companionObject!!.declaredMembers.first { it.name == "invoke" }
                    ctor.call(strValue) as Either<Error.InvalidInput, *>
                }

                when (propertyValueCreated) {
                    is Either.Left -> raise(Error.FailedToCreateObject(propertyValueCreated.value.localizedMessage))
                    is Either.Right -> propertyValueCreated.value.bind()
                }
            }

        return try {
            T::class.primaryConstructor!!.call(privateCtorParamValues)
        } catch (e: Exception) {
            raise(Error.FailedToCreateObject(e.localizedMessage))
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