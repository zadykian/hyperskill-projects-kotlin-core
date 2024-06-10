package contacts.dynamic.annotations

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class DisplayName(val name: String)
