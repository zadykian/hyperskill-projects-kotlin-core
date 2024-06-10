package contacts.dynamic

import arrow.core.Either
import arrow.core.Ior
import arrow.core.left
import arrow.core.raise.ior
import arrow.core.right
import contacts.Error
import contacts.RaiseAnyError
import contacts.dynamic.ObjectInitializer.Invoker
import contacts.dynamic.annotations.DisplayName
import contacts.dynamic.annotations.DynamicallyInvokable
import contacts.dynamic.annotations.Optional
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.jvmErasure

typealias PropertyName = String

object ObjectInitializer {
    inline fun <reified T : Any> createNew(noinline valueReader: (PropertyName) -> String): Ior<Error, T> =
        ior(Error::combine) {
            val (invoker, params) = getDynamicObjectInvoker(T::class).bind()
            val invokerArgValues = getInvokerArgValues(params, valueReader).bind()
            invoker.invoke(invokerArgValues) as T
        }

    context(RaiseAnyError)
    fun getInvokerArgValues(
        params: List<InvokerWithParams.Param>,
        valueReader: (PropertyName) -> String
    ): Ior<Error, Array<Any?>> = ior(Error::combine) {
        val iorParams = params
            .map { param -> getParameterValue({ valueReader(param.name) }, param) }

        iorParams
            .bindAll()
            .toTypedArray()
    }

    @Suppress("UNCHECKED_CAST")
    private fun getParameterValue(valueReader: () -> String, param: InvokerWithParams.Param): Ior<Error, Any?> =
        ior(Error::combine) {
            val (invoker, _) = getDynamicObjectInvoker(param.type).bind()
            val strValue = valueReader()
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

    fun getDynamicObjectInvoker(kClass: KClass<*>): Either<Error, InvokerWithParams> {
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

    private fun KCallable<*>.getInvokerParams(isCompanionMember: Boolean) =
        parameters
            .drop(if (isCompanionMember) 1 else 0)
            .map { param ->
                InvokerWithParams.Param(
                    name = param.getDisplayName(),
                    type = param.type.jvmErasure,
                    isOptional = param.annotations.any { it is Optional },
                )
            }

    private fun KParameter.getDisplayName() = annotations.filterIsInstance<DisplayName>().singleOrNull()?.name ?: name!!

    data class InvokerWithParams(
        val invoker: Invoker<*>,
        val params: List<Param>
    ) {
        data class Param(val name: String, val type: KClass<*>, val isOptional: Boolean)
    }

    fun interface Invoker<T : Any> {
        fun invoke(args: Array<Any?>): T
    }
}
