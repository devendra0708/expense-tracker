package com.expensetracker.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringExpenseDao {
    @Query("SELECT * FROM recurring_expenses ORDER BY nextDueDate ASC")
    fun getAllRecurring(): Flow<List<RecurringExpense>>

    @Query("SELECT * FROM recurring_expenses WHERE id = :id")
    suspend fun getById(id: Long): RecurringExpense?

    @Query(
        "SELECT * FROM recurring_expenses WHERE isActive = 1 AND nextDueDate <= :endOfToday ORDER BY nextDueDate ASC"
    )
    suspend fun getDueExpenses(endOfToday: Long): List<RecurringExpense>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recurring: RecurringExpense): Long

    @Update
    suspend fun update(recurring: RecurringExpense)

    @Delete
    suspend fun delete(recurring: RecurringExpense)
}
