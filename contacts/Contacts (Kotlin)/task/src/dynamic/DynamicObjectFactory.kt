package contacts.dynamic

import arrow.core.Either
import arrow.core.Ior
import arrow.core.left
import arrow.core.raise.either
import arrow.core.raise.ior
import contacts.Error
import contacts.RaiseAnyError
import contacts.dynamic.DynamicObjectInvoker.getInvoker
import kotlin.reflect.KClass
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberFunctions

typealias ValueReader = PropOrParamMetadata.PropertyContext.() -> String

@Suppress("UNCHECKED_CAST")
object DynamicObjectFactory {
    fun <T : Any> KClass<T>.new(valueReader: ValueReader) = ior(Error::combine) {
        val (invoker, params) = this@new.getInvoker().bind()
        val invokerArgValues = getInvokerArgValues(params, valueReader).bind()
        invoker.invoke(invokerArgValues) as T
    }

    fun <T : Any> T.with(targetPropName: String, valueReader: ValueReader) = either {
        val obj = this@with
        if (!obj::class.isData) {
            return Errors.isNotDataClass(obj::class).left()
        }

        Either
            .catch { callCopyFunction(targetPropName, obj, valueReader).bind() as T }
            .mapLeft { Errors.exceptionOccurred(it) }
            .bind()
    }

    private fun callCopyFunction(
        targetPropName: String,
        entity: Any,
        valueReader: ValueReader,
    ) = either {
        val entityType = entity::class
        val (_, invokerParams) = entityType.getInvoker().bind()

        val param = invokerParams
            .singleOrNull { it.displayName == targetPropName }
            ?: raise(Errors.propertyIsNotDeclaredInType(targetPropName, entityType))

        val newPropertyValue = getValue(valueReader, param).toEither().bind()

        val copyMethod = entityType.memberFunctions.single { it.name == "copy" }
        val targetParam = copyMethod.parameters.single { it.name == param.originalName }

        val params = mapOf(
            copyMethod.instanceParameter!! to entity,
            targetParam to newPropertyValue
        )

        copyMethod.callBy(params)
    }

    context(RaiseAnyError)
    private fun getInvokerArgValues(
        params: List<PropOrParamMetadata>,
        valueReader: ValueReader
    ) = ior(Error::combine) {
        params
            .map { param -> getValue(valueReader, param) }
            .bindAll()
            .toTypedArray()
    }

    private fun getValue(valueReader: ValueReader, param: PropOrParamMetadata) = ior(Error::combine) {
        val strValue = valueReader(param.context)

        if (param.type == String::class) {
            return@ior strValue
        }

        val (invoker, _) = param.type.getInvoker().bind()
        val invokerResult = invoker.invoke(arrayOf(strValue))

        if (invokerResult !is Either<*, *>) {
            return@ior invokerResult
        }

        when (val either = invokerResult as Either<Error, Any>) {
            is Either.Right -> either.value
            is Either.Left -> {
                val error = Error.InvalidInput("Bad ${param.displayName}!")
                if (param.isOptional) Ior.Both(error, null).bind()
                else raise(error)
            }
        }
    }

    private object Errors {
        fun isNotDataClass(type: KClass<*>) =
            Error.DynamicInvocationFailed("Type '${type.qualifiedName}' is expected to be a data class")

        fun exceptionOccurred(it: Throwable) =
            Error.DynamicInvocationFailed(it.message ?: "Dynamic invocation failed")

        fun propertyIsNotDeclaredInType(
            targetPropName: String,
            entityType: KClass<*>
        ) = Error.InvalidInput("'$targetPropName' is not declared in type '${entityType.simpleName}'")
    }
}
