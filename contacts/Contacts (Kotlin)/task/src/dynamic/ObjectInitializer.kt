package contacts.dynamic

import arrow.core.Either
import arrow.core.flatten
import arrow.core.raise.either
import contacts.Error
import contacts.RaiseAnyError
import contacts.RaiseDynamicInvocationFailed
import contacts.dynamic.ObjectInitializer.Invoker
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
    inline fun <reified T : Any> createNew(noinline valueReader: (PropertyName) -> String): Either<Error, T> =
        either {
            Either.catch {
                val (invoker, params) = getDynamicObjectInvoker(T::class)
                val invokerArgValues = getInvokerArgValues(params, valueReader)
                invoker.invoke(invokerArgValues) as T
            }.mapLeft { Error.DynamicInvocationFailed(it.message ?: "Error occurred: ${it::class.qualifiedName}") }
        }.flatten()


    context(RaiseAnyError)
    fun getInvokerArgValues(
        params: List<InvokerWithParams.Param>,
        valueReader: (PropertyName) -> String
    ): Array<Any?> = params
        .map { (displayName, type) -> getParameterValue({ valueReader(displayName) }, type) }
        .toTypedArray()

    context(RaiseAnyError)
    @Suppress("UNCHECKED_CAST")
    private fun getParameterValue(valueReader: () -> String, paramType: KClass<*>): Any? {
        val (invoker, _) = getDynamicObjectInvoker(paramType)
        val strValue = valueReader()
        val invokerResult = invoker.invoke(arrayOf(strValue))

        return if (invokerResult is Either<*, *>) (invokerResult as Either<Error, *>).bind()
        else invokerResult
    }

    context(RaiseDynamicInvocationFailed)
    fun getDynamicObjectInvoker(kClass: KClass<*>): InvokerWithParams {
        val ctor = kClass
            .constructors
            .firstOrNull { ctor -> ctor.isDynamicallyInvokable() }

        if (ctor != null) {
            val ctorInvoker = Invoker { args -> ctor.call(*args) }
            val ctorParams = ctor.getInvokerParams(isCompanionMember = false)
            return InvokerWithParams(ctorInvoker, ctorParams)
        }

        val companion = kClass.companionObject
            ?: raise(
                Error.DynamicInvocationFailed("Type ${kClass.simpleName} is expected to have a companion object")
            )

        val invokeFun = companion.declaredMembers.firstOrNull { isAnnotatedInvokeOperator(kClass, it) }
            ?: raise(
                Error.DynamicInvocationFailed(
                    "Type ${kClass.simpleName} is expected to have either constructor or companion "
                            + "function 'invoke' annotated with @${DynamicallyInvokable::class.simpleName}"
                )
            )

        val operatorInvoker = Invoker { args -> invokeFun.call(companion.objectInstance, *args)!! }
        val operatorParams = invokeFun.getInvokerParams(isCompanionMember = true)
        return InvokerWithParams(operatorInvoker, operatorParams)
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
                InvokerWithParams.Param(name = param.getDisplayName(), type = param.type.jvmErasure)
            }

    private fun KParameter.getDisplayName() = annotations.filterIsInstance<DisplayName>().singleOrNull()?.name ?: name!!

    data class InvokerWithParams(
        val invoker: Invoker<*>,
        val params: List<Param>
    ) {
        data class Param(val name: String, val type: KClass<*>)
    }

    fun interface Invoker<T : Any> {
        fun invoke(args: Array<Any?>): T
    }
}
