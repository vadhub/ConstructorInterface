package com.vlg.constructorinterface.table

import android.content.Context
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vlg.constructorinterface.filemanager.FileManager

class TableDataManager(private val context: Context) {

    companion object {
        private const val TABLE_DATA_FILE = "table_data.json"
    }

    fun getListNamesTables() = TABLE_DATA_FILE // future: list from file

    private val gson = Gson()

    // Автоматически определяем схему на основе содержимого интерфейса
    fun autoDetectSchemaAndSave(workArea: LinearLayout): Boolean {
        return try {
            val (columns, rows) = analyzeInterface(workArea)
            val schema = TableSchema(columns, rows)
            saveTableSchema(schema)
            true
        } catch (e: Exception) {
            Log.e("TableDataManager", "Ошибка анализа интерфейса", e)
            false
        }
    }

    // Анализ интерфейса для определения структуры
    private fun analyzeInterface(workArea: LinearLayout): Pair<List<ColumnInfo>, List<TableRow>> {
        val columnMap = mutableMapOf<String, ColumnInfo>()
        val rows = mutableListOf<TableRow>()

        // Проходим по всем строкам
        for (rowIndex in 0 until workArea.childCount) {
            val rowView = workArea.getChildAt(rowIndex)

            if (rowView is LinearLayout && rowView.orientation == LinearLayout.HORIZONTAL) {
                val rowValues = mutableMapOf<String, String>()

                // Проходим по всем элементам в строке
                for (colIndex in 0 until rowView.childCount) {
                    val element = rowView.getChildAt(colIndex)

                    when {
                        element is EditText -> {
                            val hint = element.hint?.toString() ?: "Поле ${colIndex + 1}"
                            val value = element.text?.toString() ?: ""
                            val columnId = element.tag.toString()

                            // Если столбец еще не зарегистрирован
                            if (!columnMap.containsKey(columnId)) {
                                columnMap[columnId] = ColumnInfo(
                                    id = columnId,
                                    name = hint,
                                    type = "text",
                                    order = colIndex
                                )
                            }

                            // Сохраняем значение
                            rowValues[columnId] = value
                        }
                        // Можно добавить обработку других типов полей
                    }
                }

                // Добавляем строку в результаты
                if (rowValues.isNotEmpty()) {
                    rows.add(TableRow(rowIndex, rowValues))
                }
            }
        }

        // Сортируем столбцы по порядку
        val columns = columnMap.values.sortedBy { it.order }

        Log.d("TableData", "Определено столбцов: ${columns.size}, строк: ${rows.size}")
        return Pair(columns, rows)
    }

    // Сохранение структуры таблицы
    fun saveTableSchema(schema: TableSchema): Boolean {
        return try {
            val json = gson.toJson(schema)
            FileManager.saveToFile(context, TABLE_DATA_FILE, json)
            Log.d("TableData", "Схема сохранена: ${schema.columns.size} столбцов, ${schema.rows.size} строк")
            true
        } catch (e: Exception) {
            Log.e("TableData", "Ошибка сохранения схемы", e)
            false
        }
    }

    // Загрузка структуры таблицы
    fun loadTableSchema(): TableSchema? {
        return try {
            val json = FileManager.loadFromFile(context, TABLE_DATA_FILE)
            if (json.isNullOrEmpty()) return null

            val type = object : TypeToken<TableSchema>() {}.type
            gson.fromJson<TableSchema>(json, type)
        } catch (e: Exception) {
            Log.e("TableData", "Ошибка загрузки схемы", e)
            null
        }
    }

    // Сохранение данных в CSV формате
    fun exportToCsv(schema: TableSchema): String {
        val csvBuilder = StringBuilder()

        // Заголовок
        val headers = schema.columns.joinToString("|") { it.name }
        csvBuilder.append("id|$headers\n")

        // Данные
        schema.rows.forEach { row ->
            val rowData = StringBuilder()
            rowData.append("${row.rowId}|")

            schema.columns.forEachIndexed { index, column ->
                val value = row.values[column.id] ?: ""
                rowData.append(value)
                if (index < schema.columns.size - 1) {
                    rowData.append("|")
                }
            }

            csvBuilder.append("$rowData\n")
        }

        return csvBuilder.toString()
    }

    // Экспорт в SQL формат
    fun exportToSql(schema: TableSchema, tableName: String = "user_data"): String {
        val sqlBuilder = StringBuilder()

        // Создание таблицы
        sqlBuilder.append("CREATE TABLE IF NOT EXISTS $tableName (\n")
        sqlBuilder.append("  id INTEGER PRIMARY KEY,\n")

        schema.columns.forEachIndexed { index, column ->
            val type = when (column.type) {
                "number" -> "INTEGER"
                else -> "TEXT"
            }
            sqlBuilder.append("  ${column.name.replace(" ", "_").toLowerCase()} $type")
            if (index < schema.columns.size - 1) {
                sqlBuilder.append(",\n")
            }
        }

        sqlBuilder.append("\n);\n\n")

        // Вставка данных
        schema.rows.forEach { row ->
            val columns = schema.columns.joinToString(", ") { it.name.replace(" ", "_").toLowerCase() }
            val values = schema.columns.joinToString(", ") { column ->
                val value = row.values[column.id] ?: ""
                if (value.isEmpty()) "NULL" else "'${value.replace("'", "''")}'"
            }

            sqlBuilder.append("INSERT INTO $tableName (id, $columns) VALUES (${row.rowId}, $values);\n")
        }

        return sqlBuilder.toString()
    }

    // Получить данные в виде списка Map (для отображения)
    fun getDataAsList(schema: TableSchema): List<Map<String, Any>> {
        return schema.rows.map { row ->
            mutableMapOf<String, Any>().apply {
                put("id", row.rowId)
                schema.columns.forEach { column ->
                    put(column.name, row.values[column.id] ?: "")
                }
            }
        }
    }

    // Обновить значение в таблице
    fun updateValue(schema: TableSchema, rowId: Int, columnId: String, value: String): TableSchema {
        val updatedRows = schema.rows.map { row ->
            if (row.rowId == rowId) {
                val updatedValues = row.values.toMutableMap()
                updatedValues[columnId] = value
                row.copy(values = updatedValues)
            } else {
                row
            }
        }

        return schema.copy(rows = updatedRows)
    }

    // Добавить новую строку
    fun addNewRow(schema: TableSchema?, newRowId: Int, values: Map<String, String>): TableSchema {
        val newRow = TableRow(newRowId, values)
        val updatedRows = schema?.rows?.toMutableList()?.apply {
            add(newRow)
            sortBy { it.rowId }
        }

        return schema?.copy(rows = updatedRows?: emptyList())?: TableSchema(emptyList(), emptyList())
    }

    fun removeRow(schema: TableSchema, rowId: Int): TableSchema {
        val updatedRows = schema.rows.filter { it.rowId != rowId }
        return schema.copy(rows = updatedRows)
    }

    private fun generateColumnId(name: String): String {
        return name
            .toLowerCase()
            .replace("[^a-z0-9]".toRegex(), "_")
            .takeIf { it.isNotEmpty() } ?: "column_${System.currentTimeMillis()}"
    }
}