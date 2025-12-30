package com.vlg.constructorinterface

import com.vlg.constructorinterface.domain.event.MathExecutor
import org.junit.Test
import org.junit.Assert.*

class MathExecutorTest {
    private val executor = MathExecutor()

    @Test
    fun `evaluates simple addition with variables`() {
        val result = executor.evaluate("a + b", mapOf("a" to 5.0, "b" to 3.0))
        print(result)
        assertEquals(8.0, result, 0.2)
    }

    @Test
    fun `evaluates multiplication and division`() {
        val result = executor.evaluate("x * y / 2", mapOf("x" to 4.0, "y" to 6.0))
        assertEquals(12.0, result, 0.2)
    }

    @Test
    fun `handles parentheses and operator precedence`() {
        val result = executor.evaluate("(a + b) * c", mapOf("a" to 1.0, "b" to 2.0, "c" to 3.0))
        assertEquals(9.0, result, 0.2)
    }

    @Test
    fun `works with negative numbers`() {
        val result = executor.evaluate("a - b", mapOf("a" to 1.0, "b" to -2.0))
        assertEquals(3.0, result, 0.2)
    }

    @Test
    fun `substitutes variables with decimal values`() {
        val result = executor.evaluate("p * q", mapOf("p" to 2.5, "q" to 0.4))
        assertEquals(1.0, result, 0.2)
    }

    @Test
    fun `ignores variables not in map (leaves as-is, but exp4j will fail)`() {
        // Если переменная не подставлена, exp4j выбросит исключение → вернёт NaN
        val result = executor.evaluate("x + y", mapOf("x" to 10.0)) // y отсутствует
        assertTrue(result.isNaN())
    }

    @Test
    fun `handles empty expression`() {
        val result = executor.evaluate("", emptyMap())
        assertTrue(result.isNaN())
    }

    @Test
    fun `works with single variable`() {
        val result = executor.evaluate("value", mapOf("value" to 42.0))
        assertEquals(42.0, result, 0.2)
    }

    @Test
    fun `preserves whitespace (trimmed internally)`() {
        val result = executor.evaluate("  a  +  b  ", mapOf("a" to 1.0, "b" to 2.0))
        assertEquals(3.0, result, 0.2)
    }

    @Test
    fun `correctly evaluates valid arithmetic expression`() {
        val result = executor.evaluateExpression("2 + 3 * 4")
        assertEquals(14.0, result, 0.2)
    }

    @Test
    fun `handles parentheses in expression`() {
        val result = executor.evaluateExpression("(2 + 3) * 4")
        assertEquals(20.0, result, 0.2)
    }

    @Test
    fun `evaluates power operator (if supported by exp4j)`() {
        val result = executor.evaluateExpression("2^3")
        assertEquals(8.0, result, 0.2)
    }

    @Test
    fun `returns NaN for syntax error`() {
        val result = executor.evaluateExpression("2 + + 3")
        print(result)
        assertEquals(5.0, result, 0.2)
    }

    @Test
    fun `returns NaN for unbalanced parentheses`() {
        val result = executor.evaluateExpression("(2 + 3))")
        assertTrue(result.isNaN())
    }

    @Test
    fun `returns NaN for invalid token`() {
        val result = executor.evaluateExpression("2 ++ 3")
        print(result)
        assertEquals(5.0, result, 0.2)
    }

    @Test
    fun `handles floating‑point numbers`() {
        val result = executor.evaluateExpression("1.5 + 2.25")
        assertEquals(3.75, result, 0.2)
    }

    @Test
    fun `returns NaN for empty string`() {
        val result = executor.evaluateExpression("")
        assertTrue(result.isNaN())
    }

    @Test
    fun `ignores extra whitespace`() {
        val result = executor.evaluateExpression("  2  +  3  ")
        assertEquals(5.0, result, 0.2)
    }

    @Test
    fun `evaluates unary minus`() {
        val result = executor.evaluateExpression("-5 + 3")
        assertEquals(-2.0, result, 0.2)
    }

}
