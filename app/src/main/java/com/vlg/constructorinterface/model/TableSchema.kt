package com.vlg.constructorinterface.model

// Структура таблицы
data class TableSchema(
    val columns: List<ColumnInfo>,
    val rows: List<TableRow>
)

data class ColumnInfo(
    val id: String,           // Уникальный ID столбца
    val name: String,         // Отображаемое имя (например, "ФИО")
    val type: String,         // Тип данных ("text", "number", etc.)
    val order: Int            // Порядок столбца
)

data class TableRow(
    val rowId: Int,           // ID строки в интерфейсе
    val values: Map<String, String> // ColumnId -> Value
)

fun TableSchema.getFormattedTableAligned(): String {
    val sortedColumns = columns.sortedBy { it.order }

    // Собираем все значения для каждого столбца для расчета ширины
    val columnData = sortedColumns.associate { column ->
        column.id to mutableListOf(column.name)
    }

    // Добавляем данные из строк
    rows.sortedBy { it.rowId }.forEach { row ->
        sortedColumns.forEach { column ->
            columnData[column.id]?.add(row.values[column.id] ?: "")
        }
    }

    // Рассчитываем максимальную ширину для каждого столбца
    val columnWidths = sortedColumns.associate { column ->
        column.id to (columnData[column.id]?.maxOfOrNull { it.length } ?: 0)
    }

    // Форматируем строки с выравниванием
    val formattedHeader = sortedColumns.joinToString(" | ") { column ->
        column.name.padEnd(columnWidths[column.id] ?: 0)
    }

    val separator = "-".repeat(formattedHeader.length)

    val formattedRows = rows.sortedBy { it.rowId }.map { row ->
        sortedColumns.joinToString(" | ") { column ->
            (row.values[column.id] ?: "").padEnd(columnWidths[column.id] ?: 0)
        }
    }

    return listOf(formattedHeader, separator)
        .plus(formattedRows)
        .joinToString("\n")
}