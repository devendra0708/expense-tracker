package com.expensetracker.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: ExpenseCategory,
    val date: Long = System.currentTimeMillis(),
    val note: String = "",
    val recurringId: Long? = null
)

enum class ExpenseCategory(val label: String) {
    FOOD("Food"),
    TRANSPORT("Transport"),
    SHOPPING("Shopping"),
    BILLS("Bills"),
    ENTERTAINMENT("Entertainment"),
    HEALTH("Health"),
    OTHER("Other")
}
