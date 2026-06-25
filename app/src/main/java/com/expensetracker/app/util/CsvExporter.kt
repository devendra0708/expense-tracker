package com.expensetracker.app.util

import com.expensetracker.app.data.Expense
import com.expensetracker.app.ui.formatDate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {
    fun expensesToCsv(expenses: List<Expense>): String {
        val sb = StringBuilder()
        sb.appendLine("ID,Title,Amount,Category,Date,Note")
        expenses.sortedByDescending { it.date }.forEach { expense ->
            sb.appendLine(
                listOf(
                    expense.id.toString(),
                    escapeCsv(expense.title),
                    expense.amount.toString(),
                    escapeCsv(expense.category.label),
                    escapeCsv(formatDate(expense.date)),
                    escapeCsv(expense.note)
                ).joinToString(",")
            )
        }
        return sb.toString()
    }

    fun defaultFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "expenses_$timestamp.csv"
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(',') || value.contains('"') || value.contains('\n')) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
