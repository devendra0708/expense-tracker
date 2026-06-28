package com.expensetracker.app

import android.app.Application
import androidx.room.Room
import com.expensetracker.app.data.ExpenseDatabase
import com.expensetracker.app.data.ExpenseRepository
import com.expensetracker.app.data.MIGRATION_1_2
import com.expensetracker.app.data.UserSettingsRepository

class ExpenseTrackerApp : Application() {
    val database: ExpenseDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            ExpenseDatabase::class.java,
            "expense_database"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    val repository: ExpenseRepository by lazy {
        ExpenseRepository(
            expenseDao = database.expenseDao(),
            budgetDao = database.budgetDao(),
            recurringDao = database.recurringExpenseDao()
        )
    }

    val userSettingsRepository: UserSettingsRepository by lazy {
        UserSettingsRepository(applicationContext)
    }
}
