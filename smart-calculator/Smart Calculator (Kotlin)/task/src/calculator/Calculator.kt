package calculator

object Calculator {
    fun evaluate(expr: Expression): Int =
        when (expr) {
            is Expression.Number -> expr.value.toInt()
            is Expression.Unary -> when (expr.opertr) {
                Operator.Plus -> evaluate(expr.operand)
                Operator.Minus -> evaluate(expr.operand).unaryMinus()
            }

            is Expression.Binary -> when (expr.opertr) {
                Operator.Plus -> evaluate(expr.left) + evaluate(expr.right)
                Operator.Minus -> evaluate(expr.left) - evaluate(expr.right)
            }
        }
}
