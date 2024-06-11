package contacts.dynamic

import contacts.RaiseAnyError
import contacts.dynamic.DynamicObjectScanner.getProperties
import kotlin.reflect.full.declaredMemberProperties

object DynamicStringBuilder {
    context(RaiseAnyError)
    fun asString(obj: Any): String {
        val namesToProps = obj::class.declaredMemberProperties.associateBy { it.name }

        return obj::class
            .getProperties()
            .bind()
            .joinToString(separator = "\n") {
                val property = namesToProps.getValue(it.originalName)
                val displayName = it.displayName.replaceFirstChar { c -> c.uppercase() }
                val defaultValue = "[no data]"

                // https://youtrack.jetbrains.com/issue/KT-67026
                val value = try {
                    property.getter.call(obj)?.toString() ?: defaultValue
                } catch (e: Exception) {
                    defaultValue
                }

                "$displayName: $value"
            }
    }
}
