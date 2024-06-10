package contacts.dynamic.annotations

@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class DisplayOrder(val order: Int)