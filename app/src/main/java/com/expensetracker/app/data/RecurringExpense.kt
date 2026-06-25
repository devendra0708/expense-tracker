package com.expensetracker.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_expenses")
data class RecurringExpense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: ExpenseCategory,
    val frequency: RecurrenceFrequency,
    val startDate: Long,
    val nextDueDate: Long,
    val note: String = "",
    val isActive: Boolean = true
)

enum class RecurrenceFrequency(val label: String) {
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}
