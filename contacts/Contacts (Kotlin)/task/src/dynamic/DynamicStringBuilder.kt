package contacts.dynamic

import contacts.RaiseAnyError
import kotlin.reflect.full.memberProperties

object DynamicStringBuilder {
    context(RaiseAnyError)
    inline fun <reified T : Any> asString(obj: T): String {
        val namesToProps = T::class.memberProperties.associateBy { it.name }

        return DynamicObjectFactory
            .propsOf(obj::class)
            .bind()
            .joinToString {
                val property = namesToProps.getValue(it.originalName)
                val displayName = it.displayName.replaceFirstChar { it.uppercase() }
                val value = property.getter.invoke(obj)?.toString() ?: "[no data]"
                "$displayName: $value"
            }
    }
}
