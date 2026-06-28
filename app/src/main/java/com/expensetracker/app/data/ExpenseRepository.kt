package com.expensetracker.app.data

import com.expensetracker.app.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val budgetDao: BudgetDao,
    private val recurringDao: RecurringExpenseDao
) {
    private val recurringProcessingMutex = Mutex()

    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    val totalSpent: Flow<Double?> = expenseDao.getTotalSpent()
    val categoryTotals: Flow<List<CategoryTotal>> = expenseDao.getCategoryTotals()
    val recurringExpenses: Flow<List<RecurringExpense>> = recurringDao.getAllRecurring()

    fun monthlySpent(year: Int, month: Int): Flow<Double> {
        val start = DateUtils.monthStartMillis(year, month)
        val end = DateUtils.monthEndMillis(year, month)
        return expenseDao.getSpentInRange(start, end).map { it ?: 0.0 }
    }

    fun categoryTotalsForMonth(year: Int, month: Int): Flow<List<CategoryTotal>> {
        val start = DateUtils.monthStartMillis(year, month)
        val end = DateUtils.monthEndMillis(year, month)
        return expenseDao.getCategoryTotalsInRange(start, end)
    }

    fun monthlyBudget(year: Int, month: Int): Flow<MonthlyBudget?> =
        budgetDao.getMonthlyBudget(year, month)

    fun categoryBudgets(year: Int, month: Int): Flow<List<CategoryBudget>> =
        budgetDao.getCategoryBudgets(year, month)

    suspend fun getMonthlyTotalsForLastMonths(months: List<Pair<Int, Int>>): List<MonthlyTotal> {
        return months.map { (year, month) ->
            val start = DateUtils.monthStartMillis(year, month)
            val end = DateUtils.monthEndMillis(year, month)
            val total = expenseDao.getSpentInRangeOnce(start, end) ?: 0.0
            MonthlyTotal(year, month, total)
        }
    }

    suspend fun getExpenseById(id: Long): Expense? = expenseDao.getExpenseById(id)

    suspend fun getRecurringById(id: Long): RecurringExpense? = recurringDao.getById(id)

    suspend fun insert(expense: Expense): Long = expenseDao.insert(expense)

    suspend fun update(expense: Expense) = expenseDao.update(expense)

    suspend fun delete(expense: Expense) = expenseDao.delete(expense)

    suspend fun saveMonthlyBudget(year: Int, month: Int, amount: Double) {
        budgetDao.upsertMonthlyBudget(MonthlyBudget(year, month, amount))
    }

    suspend fun saveCategoryBudgets(year: Int, month: Int, budgets: Map<ExpenseCategory, Double>) {
        val entities = budgets
            .filter { it.value > 0 }
            .map { (category, amount) ->
                CategoryBudget(year, month, category, amount)
            }
        budgetDao.upsertCategoryBudgets(entities)
    }

    suspend fun insertRecurring(recurring: RecurringExpense): Long = recurringDao.insert(recurring)

    suspend fun updateRecurring(recurring: RecurringExpense) = recurringDao.update(recurring)

    suspend fun deleteRecurring(recurring: RecurringExpense) = recurringDao.delete(recurring)

    suspend fun processDueRecurringExpenses() {
        recurringProcessingMutex.withLock {
            val due = recurringDao.getDueExpenses(DateUtils.endOfTodayMillis())
            for (recurring in due) {
                var dueDate = recurring.nextDueDate
                val endOfToday = DateUtils.endOfTodayMillis()
                while (dueDate <= endOfToday) {
                    expenseDao.insert(
                        Expense(
                            title = recurring.title,
                            amount = recurring.amount,
                            category = recurring.category,
                            date = dueDate,
                            note = recurring.note,
                            recurringId = recurring.id
                        )
                    )
                    dueDate = DateUtils.calculateNextDueDate(dueDate, recurring.frequency)
                }
                recurringDao.update(recurring.copy(nextDueDate = dueDate))
            }
        }
    }

    fun budgetStatus(year: Int, month: Int): Flow<BudgetStatus> {
        return combine(
            monthlyBudget(year, month),
            monthlySpent(year, month),
            categoryBudgets(year, month),
            categoryTotalsForMonth(year, month)
        ) { monthlyBudget, spent, categoryBudgets, categoryTotals ->
            BudgetStatus(
                monthlyBudget = monthlyBudget?.amount,
                monthlySpent = spent,
                categoryBudgets = categoryBudgets.associate { it.category to it.amount },
                categorySpent = categoryTotals.associate { it.category to it.total }
            )
        }
    }
}

data class BudgetStatus(
    val monthlyBudget: Double?,
    val monthlySpent: Double,
    val categoryBudgets: Map<ExpenseCategory, Double>,
    val categorySpent: Map<ExpenseCategory, Double>
)
