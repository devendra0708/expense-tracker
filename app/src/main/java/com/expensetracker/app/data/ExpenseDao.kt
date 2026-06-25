package com.expensetracker.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): Expense?

    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalSpent(): Flow<Double?>

    @Query("SELECT category, SUM(amount) as total FROM expenses GROUP BY category")
    fun getCategoryTotals(): Flow<List<CategoryTotal>>

    @Query(
        "SELECT SUM(amount) FROM expenses WHERE date >= :startMillis AND date <= :endMillis"
    )
    fun getSpentInRange(startMillis: Long, endMillis: Long): Flow<Double?>

    @Query(
        "SELECT category, SUM(amount) as total FROM expenses WHERE date >= :startMillis AND date <= :endMillis GROUP BY category"
    )
    fun getCategoryTotalsInRange(startMillis: Long, endMillis: Long): Flow<List<CategoryTotal>>

    @Query(
        "SELECT SUM(amount) as total FROM expenses WHERE date >= :startMillis AND date <= :endMillis"
    )
    suspend fun getSpentInRangeOnce(startMillis: Long, endMillis: Long): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)
}

data class CategoryTotal(
    val category: ExpenseCategory,
    val total: Double
)

data class MonthlyTotal(
    val year: Int,
    val month: Int,
    val total: Double
)
