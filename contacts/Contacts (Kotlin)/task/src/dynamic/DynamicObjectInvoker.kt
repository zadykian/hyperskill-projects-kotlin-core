package contacts.dynamic

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import contacts.Error
import contacts.RaiseDynamicInvocationFailed
import contacts.dynamic.DynamicObjectScanner.getDisplayName
import contacts.dynamic.annotations.DynamicallyInvokable
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.jvmErasure

data class PropOrParamMetadata(
    val displayName: String, val originalName: String, val type: KClass<*>, val isOptional: Boolean
) {
    val context = PropertyContext(displayName)

    class PropertyContext(val propertyName: String)
}

fun interface Invoker<T : Any> {
    fun invoke(args: Array<Any?>): T
}

data class InvokerWithParams(val invoker: Invoker<*>, val params: List<PropOrParamMetadata>)

object DynamicObjectInvoker {
    fun KClass<*>.getInvoker(): Either<Error, InvokerWithParams> = either {
        val type = this@getInvoker
        val ctor = type
            .constructors
            .firstOrNull { ctor -> ctor.isDynamicallyInvokable() }

        if (ctor != null) {
            val ctorInvoker = Invoker { args -> ctor.call(*args) }
            val ctorParams = ctor.getInvokerParams(isCompanionMember = false)
            return InvokerWithParams(ctorInvoker, ctorParams).right()
        }

        val companion = type.companionObject
            ?: return Errors.companionObjectIsMissing(type).left()

        val invokeFun = companion.declaredMembers.firstOrNull { isAnnotatedInvokeOperator(type, it) }
            ?: return Errors.ctorAndInvokeFunAreMissing(type).left()

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
                PropOrParamMetadata(
                    displayName = param.getDisplayName(),
                    originalName = param.name
                        ?: raise(Errors.unnamedParameter(param)),
                    type = param.type.jvmErasure,
                    isOptional = param.type.isMarkedNullable,
                )
            }

    private object Errors {
        fun companionObjectIsMissing(kClass: KClass<*>) = Error.DynamicInvocationFailed(
            "Type ${kClass.simpleName} is expected to have a companion object"
        )

        fun ctorAndInvokeFunAreMissing(kClass: KClass<*>) = Error.DynamicInvocationFailed(
            "Type ${kClass.simpleName} is expected to have either constructor or companion "
                    + "function 'invoke' annotated with @${DynamicallyInvokable::class.simpleName}"
        )

        fun unnamedParameter(param: KParameter) =
            Error.DynamicInvocationFailed("Parameter $param is expected to have a name")
    }
}
