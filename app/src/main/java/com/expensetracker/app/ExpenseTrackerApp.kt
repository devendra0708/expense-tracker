package com.expensetracker.app

import android.app.Application
import androidx.room.Room
import com.expensetracker.app.data.ExpenseDatabase
import com.expensetracker.app.data.ExpenseRepository
import com.expensetracker.app.data.MIGRATION_1_2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ExpenseTrackerApp : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            repository.processDueRecurringExpenses()
        }
    }
}
