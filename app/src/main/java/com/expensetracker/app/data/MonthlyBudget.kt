package com.expensetracker.app.data

import androidx.room.Entity

@Entity(
    tableName = "monthly_budgets",
    primaryKeys = ["year", "month"]
)
data class MonthlyBudget(
    val year: Int,
    val month: Int,
    val amount: Double
)

@Entity(
    tableName = "category_budgets",
    primaryKeys = ["year", "month", "category"]
)
data class CategoryBudget(
    val year: Int,
    val month: Int,
    val category: ExpenseCategory,
    val amount: Double
)
