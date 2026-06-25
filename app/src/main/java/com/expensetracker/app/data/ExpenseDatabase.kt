package com.expensetracker.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Expense::class,
        MonthlyBudget::class,
        CategoryBudget::class,
        RecurringExpense::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringExpenseDao(): RecurringExpenseDao
}

class Converters {
    @TypeConverter
    fun fromCategory(category: ExpenseCategory): String = category.name

    @TypeConverter
    fun toCategory(value: String): ExpenseCategory = ExpenseCategory.valueOf(value)

    @TypeConverter
    fun fromFrequency(frequency: RecurrenceFrequency): String = frequency.name

    @TypeConverter
    fun toFrequency(value: String): RecurrenceFrequency = RecurrenceFrequency.valueOf(value)
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE expenses ADD COLUMN recurringId INTEGER DEFAULT NULL")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS monthly_budgets (
                year INTEGER NOT NULL,
                month INTEGER NOT NULL,
                amount REAL NOT NULL,
                PRIMARY KEY(year, month)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS category_budgets (
                year INTEGER NOT NULL,
                month INTEGER NOT NULL,
                category TEXT NOT NULL,
                amount REAL NOT NULL,
                PRIMARY KEY(year, month, category)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS recurring_expenses (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                amount REAL NOT NULL,
                category TEXT NOT NULL,
                frequency TEXT NOT NULL,
                startDate INTEGER NOT NULL,
                nextDueDate INTEGER NOT NULL,
                note TEXT NOT NULL,
                isActive INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}
