package com.vlg.constructorinterface.event

import net.objecthunter.exp4j.ExpressionBuilder

class MathExecutor {

    fun evaluate(
        expression: String,
        variables: Map<String, Double>
    ): Double {

        var expr = expression.trim()

        variables.forEach { (tag, value) ->
            expr = expr.replace(tag, value.toString())
        }

        return calculate(expr)
    }

    private fun calculate(expr: String): Double {
        val clean = expr.replace("\\s+".toRegex(), "")
        return evaluateExpression(clean)
    }

    fun evaluateExpression(expression: String): Double {
        try {
            val expression = ExpressionBuilder(expression).build()
            val result = expression.evaluate()
            return result
        } catch (e: Exception) {
            println("Error evaluating expression: ${e.message}")
            return Double.NaN
        }
    }
}