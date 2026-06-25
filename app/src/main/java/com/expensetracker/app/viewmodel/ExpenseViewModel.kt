package com.expensetracker.app.viewmodel

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.data.BudgetStatus
import com.expensetracker.app.data.CategoryTotal
import com.expensetracker.app.data.Expense
import com.expensetracker.app.data.ExpenseCategory
import com.expensetracker.app.data.ExpenseRepository
import com.expensetracker.app.data.MonthlyTotal
import com.expensetracker.app.data.RecurringExpense
import com.expensetracker.app.data.RecurrenceFrequency
import com.expensetracker.app.util.CsvExporter
import com.expensetracker.app.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {
    private val selectedYearMonth = MutableStateFlow(DateUtils.currentYearMonth())

    val selectedMonth: StateFlow<Pair<Int, Int>> = selectedYearMonth.asStateFlow()

    val expenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSpent: StateFlow<Double> = repository.totalSpent
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val categoryTotals: StateFlow<List<CategoryTotal>> = repository.categoryTotals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recurringExpenses: StateFlow<List<RecurringExpense>> = repository.recurringExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlySpent: StateFlow<Double> = selectedYearMonth
        .flatMapLatest { (year, month) -> repository.monthlySpent(year, month) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlyCategoryTotals: StateFlow<List<CategoryTotal>> = selectedYearMonth
        .flatMapLatest { (year, month) -> repository.categoryTotalsForMonth(year, month) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgetStatus: StateFlow<BudgetStatus> = selectedYearMonth
        .flatMapLatest { (year, month) -> repository.budgetStatus(year, month) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            BudgetStatus(null, 0.0, emptyMap(), emptyMap())
        )

    private val _monthlyChartData = MutableStateFlow<List<MonthlyTotal>>(emptyList())
    val monthlyChartData: StateFlow<List<MonthlyTotal>> = _monthlyChartData.asStateFlow()

    private val _exportMessage = MutableStateFlow<String?>(null)
    val exportMessage: StateFlow<String?> = _exportMessage.asStateFlow()

    init {
        refreshMonthlyChartData()
        viewModelScope.launch {
            repository.processDueRecurringExpenses()
        }
    }

    fun shiftSelectedMonth(delta: Int) {
        val (year, month) = selectedYearMonth.value
        selectedYearMonth.value = DateUtils.shiftMonth(year, month, delta)
    }

    fun refreshMonthlyChartData(monthCount: Int = 6) {
        viewModelScope.launch {
            _monthlyChartData.value = repository.getMonthlyTotalsForLastMonths(
                DateUtils.lastNMonths(monthCount)
            )
        }
    }

    suspend fun getExpenseById(id: Long): Expense? = repository.getExpenseById(id)

    suspend fun getRecurringById(id: Long): RecurringExpense? = repository.getRecurringById(id)

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { repository.delete(expense) }
    }

    fun saveExpense(
        id: Long = 0,
        title: String,
        amount: Double,
        category: ExpenseCategory,
        date: Long,
        note: String
    ) {
        viewModelScope.launch {
            val expense = Expense(
                id = id,
                title = title.trim(),
                amount = amount,
                category = category,
                date = date,
                note = note.trim()
            )
            if (id == 0L) repository.insert(expense) else repository.update(expense)
        }
    }

    fun saveMonthlyBudget(amount: Double) {
        val (year, month) = selectedYearMonth.value
        viewModelScope.launch {
            repository.saveMonthlyBudget(year, month, amount)
        }
    }

    fun saveCategoryBudgets(budgets: Map<ExpenseCategory, Double>) {
        val (year, month) = selectedYearMonth.value
        viewModelScope.launch {
            repository.saveCategoryBudgets(year, month, budgets)
        }
    }

    fun saveRecurring(
        id: Long = 0,
        title: String,
        amount: Double,
        category: ExpenseCategory,
        frequency: RecurrenceFrequency,
        startDate: Long,
        note: String,
        isActive: Boolean = true
    ) {
        viewModelScope.launch {
            val recurring = if (id == 0L) {
                RecurringExpense(
                    title = title.trim(),
                    amount = amount,
                    category = category,
                    frequency = frequency,
                    startDate = startDate,
                    nextDueDate = startDate,
                    note = note.trim(),
                    isActive = isActive
                )
            } else {
                val existing = repository.getRecurringById(id) ?: return@launch
                existing.copy(
                    title = title.trim(),
                    amount = amount,
                    category = category,
                    frequency = frequency,
                    startDate = startDate,
                    note = note.trim(),
                    isActive = isActive
                )
            }
            if (id == 0L) repository.insertRecurring(recurring)
            else repository.updateRecurring(recurring)
        }
    }

    fun deleteRecurring(recurring: RecurringExpense) {
        viewModelScope.launch { repository.deleteRecurring(recurring) }
    }

    fun toggleRecurringActive(recurring: RecurringExpense) {
        viewModelScope.launch {
            repository.updateRecurring(recurring.copy(isActive = !recurring.isActive))
        }
    }

    fun processRecurringNow() {
        viewModelScope.launch { repository.processDueRecurringExpenses() }
    }

    fun exportCsv(context: Context) {
        viewModelScope.launch {
            exportCsvInternal(context, expenses.value)
        }
    }

    private suspend fun exportCsvInternal(context: Context, expenses: List<Expense>) {
        try {
            val csv = CsvExporter.expensesToCsv(expenses)
            val fileName = CsvExporter.defaultFileName()
            val dir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(dir, fileName)
            file.writeText(csv)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Expense Export")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                Intent.createChooser(shareIntent, "Export expenses as CSV")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            _exportMessage.value = "Exported ${expenses.size} expenses"
        } catch (e: Exception) {
            _exportMessage.value = "Export failed: ${e.message}"
        }
    }

    fun clearExportMessage() {
        _exportMessage.value = null
    }
}

class ExpenseViewModelFactory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            return ExpenseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
