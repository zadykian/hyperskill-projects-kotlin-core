package contacts.dynamic

import contacts.RaiseAnyError
import kotlin.reflect.full.declaredMemberProperties

object DynamicStringBuilder {
    context(RaiseAnyError)
    fun asString(obj: Any): String {
        val namesToProps = obj::class.declaredMemberProperties.associateBy { it.name }

        return DynamicObjectFactory
            .propsOf(obj::class)
            .bind()
            .joinToString(separator = "\n") {
                val property = namesToProps.getValue(it.originalName)
                val displayName = it.displayName.replaceFirstChar { c -> c.uppercase() }
                val value = property.getter.call(obj)?.toString() ?: "[no data]"
                "$displayName: $value"
            }
    }
}
