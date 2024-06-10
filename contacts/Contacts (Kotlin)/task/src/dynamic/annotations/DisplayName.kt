package contacts.dynamic.annotations

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.CLASS)
annotation class DisplayName(val name: String)
