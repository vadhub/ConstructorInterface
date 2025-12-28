package com.vlg.constructorinterface.event

import android.util.Log
import android.view.View
import com.vlg.constructorinterface.createui.extractText
import net.objecthunter.exp4j.ExpressionBuilder

class MathExecutor {

    fun substituteVariablesView(expression: String, variables: Map<String, View>): String {
        val regex = Regex("\\b[a-zA-Z][a-zA-Z0-9_]*\\b")

        return regex.replace(expression) { matchResult ->
            val varName = matchResult.value
            val view = variables[varName] ?: return@replace varName
            view.extractText() ?: return@replace varName
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
            Log.e("MathExecutor","Error evaluating expression: ${e.message}")
            return Double.NaN
        }
    }
}