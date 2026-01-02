package com.vlg.constructorinterface.domain.event

import android.util.Log
import com.vlg.constructorinterface.model.Element
import net.objecthunter.exp4j.ExpressionBuilder

class MathExecutor {

    fun substituteVariablesView(expression: String, variables: List<Element>): String {
        val variablesMap = variables.associate { it.tag to it.text }

        val regex = Regex("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b")

        return regex.replace(expression) { matchResult ->
            val varName = matchResult.value
            variablesMap[varName] ?: varName
        }
    }

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

    fun calculate(expr: String): Double {
        if (expr.isBlank()) return Double.NaN
        val clean = expr.replace("\\s+".toRegex(), "")
        return evaluateExpression(clean)
    }

    fun evaluateExpression(expression: String): Double {
        try {
            val expression = ExpressionBuilder(expression).build()
            val result = expression.evaluate()
            return result
        } catch (e: Exception) {
            Log.e("MathExecutor", "Error evaluating expression: ${e.message}")
            return Double.NaN
        }
    }
}