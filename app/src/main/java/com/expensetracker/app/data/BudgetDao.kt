package com.expensetracker.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM monthly_budgets WHERE year = :year AND month = :month")
    fun getMonthlyBudget(year: Int, month: Int): Flow<MonthlyBudget?>

    @Query("SELECT * FROM category_budgets WHERE year = :year AND month = :month")
    fun getCategoryBudgets(year: Int, month: Int): Flow<List<CategoryBudget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMonthlyBudget(budget: MonthlyBudget)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategoryBudget(budget: CategoryBudget)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategoryBudgets(budgets: List<CategoryBudget>)
}
