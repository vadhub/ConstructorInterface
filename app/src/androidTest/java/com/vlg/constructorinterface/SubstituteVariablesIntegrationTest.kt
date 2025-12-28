package com.vlg.constructorinterface

import android.content.Context
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vlg.constructorinterface.event.MathExecutor
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SubstituteVariablesIntegrationTest {
    private lateinit var context: Context
    private val mathExecutor = MathExecutor()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    // Тест 1: Базовая подстановка текста из TextView
    @Test
    fun should_substitute_TextView_text_in_expression() {
        // Arrange
        val textView = TextView(context).apply {
            text = "Hello"
        }

        val expression = "Greeting is: message"
        val variables = mapOf("message" to textView)

        // Act
        val result = mathExecutor.substituteVariablesView(expression, variables)

        // Assert
        assertEquals("Greeting is: Hello", result)
    }

    // Тест 2: Подстановка из EditText с пользовательским вводом
    @Test
    fun should_substitute_EditText_text_with_user_input() {
        // Arrange
        val editText = EditText(context).apply {
            setText("John Doe")
        }

        val expression = "User name is user_name"
        val variables = mapOf("user_name" to editText)

        // Act
        val result = mathExecutor.substituteVariablesView(expression, variables)

        // Assert
        assertEquals("User name is John Doe", result)
    }

    // Тест 3: Комплексное выражение с несколькими View
    @Test
    fun should_substitute_multiple_variables_from_different_views() {
        // Arrange
        val firstName = TextView(context).apply { text = "John" }
        val lastName = TextView(context).apply { text = "Smith" }
        val age = TextView(context).apply { text = "30" }

        val expression = "Patient: last_name, first_name. Age: age years."
        val variables = mapOf(
            "first_name" to firstName,
            "last_name" to lastName,
            "age" to age
        )

        // Act
        val result = mathExecutor.substituteVariablesView(expression, variables)

        // Assert
        assertEquals("Patient: Smith, John. Age: 30 years.", result)
    }

    // Тест 4: Обработка отсутствующих переменных
    @Test
    fun should_keep_variable_unchanged_when_not_found_in_map() {
        // Arrange
        val textView = TextView(context).apply { text = "World" }
        val expression = "Hello name, welcome to world"
        val variables = mapOf("world" to textView)

        // Act
        val result = mathExecutor.substituteVariablesView(expression, variables)

        // Assert
        assertEquals("Hello name, welcome to World", result)
    }

    // Тест 5: View с null текстом
    @Test
    fun should_keep_variable_when_extractText_returns_null() {
        // Arrange
        val emptyTextView = TextView(context) // text is null
        val expression = "Value: value"
        val variables = mapOf("value" to emptyTextView)

        // Act
        val result = mathExecutor.substituteVariablesView(expression, variables)

        print(result)

        // Assert
        assertEquals("Value: value", result)
    }

    // Тест 6: Работа с CompoundView (кнопка с текстом)
    @Test
    fun should_substitute_text_from_Button_view() {
        // Arrange
        val button = Button(context).apply {
            text = "Submit"
        }

        val expression = "Click button_name to continue"
        val variables = mapOf("button_name" to button)

        // Act
        val result = mathExecutor.substituteVariablesView(expression, variables)

        // Assert
        assertEquals("Click Submit to continue", result)
    }

    // Тест 7: Выражение с HTML тегами и переменными
    @Test
    fun should_handle_variables_in_HTML_content() {
        // Arrange
        val titleView = TextView(context).apply {
            text = "Important"
        }

        val expression = "<h1>title</h1><p>Content here</p>"
        val variables = mapOf("title" to titleView)

        // Act
        val result = mathExecutor.substituteVariablesView(expression, variables)

        // Assert
        assertEquals("<h1>Important</h1><p>Content here</p>", result)
    }

    // Тест 8: Обработка спецсимволов и пробелов
    @Test
    fun should_handle_variables_with_special_characters_around() {
        // Arrange
        val priceView = TextView(context).apply {
            text = "$100"
        }

        val expression = "Total: price! tax: price * 0.2"
        val variables = mapOf("price" to priceView)

        // Act
        val result = mathExecutor.substituteVariablesView(expression, variables)

        // Assert
        assertEquals("Total: $100! tax: $100 * 0.2", result)
    }
}