package contacts.dynamic

import arrow.core.Either
import arrow.core.Ior
import arrow.core.left
import arrow.core.raise.either
import arrow.core.raise.ior
import arrow.core.right
import contacts.Error
import contacts.RaiseAnyError
import contacts.RaiseDynamicInvocationFailed
import contacts.dynamic.DynamicObjectFactory.Invoker
import contacts.dynamic.annotations.DisplayName
import contacts.dynamic.annotations.DynamicallyInvokable
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure

typealias ValueReader = DynamicObjectFactory.Param.PropertyContext.() -> String
typealias PropertyName = String

object DynamicObjectFactory {
    inline fun <reified T : Any> createNew(noinline valueReader: ValueReader): Ior<Error, T> =
        ior(Error::combine) {
            val (invoker, params) = getDynamicObjectInvoker(T::class).bind()
            val invokerArgValues = getInvokerArgValues(params, valueReader).bind()
            invoker.invoke(invokerArgValues) as T
        }

    context(RaiseDynamicInvocationFailed)
    inline fun <reified T : Any> propertyNamesOf(): Either<Error, List<PropertyName>> = either {
        getDynamicObjectInvoker(T::class)
            .bind()
            .params
            .map { it.displayName }
    }

    inline fun <reified T : Any> copy(
        entity: T,
        targetPropName: PropertyName,
        noinline valueReader: ValueReader
    ): Either<Error, T> = either {
        if (!T::class.isData) {
            return Error.DynamicInvocationFailed("Type '${T::class.simpleName}' is expected to be a data class").left()
        }

        Either
            .catch { callCopyFunction(targetPropName, entity, T::class, valueReader).bind() as T }
            .mapLeft { Error.DynamicInvocationFailed(it.message ?: "Dynamic invocation failed") }
            .bind()
    }

    fun callCopyFunction(
        targetPropName: PropertyName,
        entity: Any,
        entityType: KClass<*>,
        valueReader: ValueReader,
    ) = either {
        val (_, invokerParams) = getDynamicObjectInvoker(entityType).bind()

        val param = invokerParams
            .singleOrNull { it.displayName == targetPropName }
            ?: raise(Error.InvalidInput("'$targetPropName' is not declared in type '${entityType.simpleName}'"))

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
    fun getInvokerArgValues(
        params: List<Param>,
        valueReader: ValueReader
    ): Ior<Error, Array<Any?>> = ior(Error::combine) {
        params
            .map { param -> getValue(valueReader, param) }
            .bindAll()
            .toTypedArray()
    }

    @Suppress("UNCHECKED_CAST")
    fun getValue(valueReader: ValueReader, param: Param): Ior<Error, Any?> =
        ior(Error::combine) {
            val strValue = valueReader(param.context)

            if (param.type == String::class) {
                return@ior strValue
            }

            val (invoker, _) = getDynamicObjectInvoker(param.type).bind()
            val invokerResult = invoker.invoke(arrayOf(strValue))

            if (invokerResult !is Either<*, *>) {
                return@ior invokerResult
            }

            when (val either = invokerResult as Either<Error, Any>) {
                is Either.Right -> either.value
                is Either.Left -> {
                    if (param.isOptional) Ior.Both(either.value, null).bind()
                    else raise(either.value)
                }
            }
        }

    fun getDynamicObjectInvoker(kClass: KClass<*>): Either<Error, InvokerWithParams> = either {
        val ctor = kClass
            .constructors
            .firstOrNull { ctor -> ctor.isDynamicallyInvokable() }

        if (ctor != null) {
            val ctorInvoker = Invoker { args -> ctor.call(*args) }
            val ctorParams = ctor.getInvokerParams(isCompanionMember = false)
            return InvokerWithParams(ctorInvoker, ctorParams).right()
        }

        val companion = kClass.companionObject
            ?: return Error
                .DynamicInvocationFailed("Type ${kClass.simpleName} is expected to have a companion object")
                .left()

        val invokeFun = companion.declaredMembers.firstOrNull { isAnnotatedInvokeOperator(kClass, it) }
            ?: return Error
                .DynamicInvocationFailed(
                    "Type ${kClass.simpleName} is expected to have either constructor or companion "
                            + "function 'invoke' annotated with @${DynamicallyInvokable::class.simpleName}"
                )
                .left()

        val operatorInvoker = Invoker { args -> invokeFun.call(companion.objectInstance, *args)!! }
        val operatorParams = invokeFun.getInvokerParams(isCompanionMember = true)
        return InvokerWithParams(operatorInvoker, operatorParams).right()
    }

    private fun isAnnotatedInvokeOperator(target: KClass<*>, function: KCallable<*>) =
        function.name == "invoke"
                && function.isDynamicallyInvokable()
                && (function.returnType == target.starProjectedType
                || function.returnType.isSubtypeOf(Either::class.starProjectedType))

    private fun KAnnotatedElement.isDynamicallyInvokable() = annotations.any { it is DynamicallyInvokable }

    context(RaiseDynamicInvocationFailed)
    private fun KCallable<*>.getInvokerParams(isCompanionMember: Boolean) =
        parameters
            .drop(if (isCompanionMember) 1 else 0)
            .map { param ->
                Param(
                    displayName = param.getDisplayName(),
                    originalName = param.name
                        ?: raise(Error.DynamicInvocationFailed("Parameter $param is expected to have a name")),
                    type = param.type.jvmErasure,
                    isOptional = param.type.isMarkedNullable,
                )
            }

    context(RaiseDynamicInvocationFailed)
    private fun KAnnotatedElement.getDisplayName(): String {
        val displayName = annotations.filterIsInstance<DisplayName>().singleOrNull()?.name

        if (displayName != null) {
            return displayName
        }

        fun raise(): Nothing = raise(Error.DynamicInvocationFailed("Failed to get a display name for $this"))

        return when (this) {
            is KParameter -> name ?: raise()
            is KProperty<*> -> name
            else -> raise()
        }
    }

    data class InvokerWithParams(val invoker: Invoker<*>, val params: List<Param>)

    data class Param(
        val displayName: PropertyName, val originalName: PropertyName, val type: KClass<*>, val isOptional: Boolean
    ) {
        val context = PropertyContext(displayName)

        class PropertyContext(val propertyName: PropertyName)
    }

    fun interface Invoker<T : Any> {
        fun invoke(args: Array<Any?>): T
    }
}
