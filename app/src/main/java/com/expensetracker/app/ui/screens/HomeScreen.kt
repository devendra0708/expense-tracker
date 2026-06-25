package com.expensetracker.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.expensetracker.app.data.CategoryTotal
import com.expensetracker.app.data.Expense
import com.expensetracker.app.ui.CategoryIcons
import com.expensetracker.app.ui.components.BudgetProgressBar
import com.expensetracker.app.ui.formatCurrency
import com.expensetracker.app.ui.formatDate
import com.expensetracker.app.util.DateUtils
import com.expensetracker.app.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ExpenseViewModel,
    onAddExpense: () -> Unit,
    onEditExpense: (Long) -> Unit
) {
    val context = LocalContext.current
    val expenses by viewModel.expenses.collectAsState()
    val monthlySpent by viewModel.monthlySpent.collectAsState()
    val categoryTotals by viewModel.monthlyCategoryTotals.collectAsState()
    val budgetStatus by viewModel.budgetStatus.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val exportMessage by viewModel.exportMessage.collectAsState()
    val (year, month) = selectedMonth

    val monthExpenses = remember(expenses, year, month) {
        val start = DateUtils.monthStartMillis(year, month)
        val end = DateUtils.monthEndMillis(year, month)
        expenses.filter { it.date in start..end }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(exportMessage) {
        exportMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearExportMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Tracker") },
                actions = {
                    IconButton(onClick = { viewModel.exportCsv(context) }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export CSV")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddExpense,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add expense")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                MonthSelector(
                    label = DateUtils.formatMonthYear(year, month),
                    onPrevious = { viewModel.shiftSelectedMonth(-1) },
                    onNext = { viewModel.shiftSelectedMonth(1) }
                )
            }

            item {
                MonthlySpentCard(
                    spent = monthlySpent,
                    budget = budgetStatus.monthlyBudget
                )
            }

            val monthlyBudget = budgetStatus.monthlyBudget
            if (monthlyBudget != null && monthlyBudget > 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Budget Progress",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            BudgetProgressBar(
                                spent = budgetStatus.monthlySpent,
                                budget = monthlyBudget
                            )
                        }
                    }
                }
            }

            if (categoryTotals.isNotEmpty()) {
                item {
                    CategoryBreakdownCard(categoryTotals, monthlySpent)
                }
            }

            item {
                Text(
                    text = "Expenses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            if (monthExpenses.isEmpty()) {
                item {
                    EmptyStateCard()
                }
            } else {
                items(monthExpenses, key = { it.id }) { expense ->
                    ExpenseCard(
                        expense = expense,
                        onClick = { onEditExpense(expense.id) },
                        onDelete = { viewModel.deleteExpense(expense) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlySpentCard(spent: Double, budget: Double?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Spent This Month",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatCurrency(spent),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (budget != null && budget > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Budget: ${formatCurrency(budget)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun CategoryBreakdownCard(
    categoryTotals: List<CategoryTotal>,
    totalSpent: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "By Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            categoryTotals.sortedByDescending { it.total }.forEach { item ->
                val fraction = if (totalSpent > 0) (item.total / totalSpent).toFloat() else 0f
                CategoryRow(item, fraction)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CategoryRow(item: CategoryTotal, fraction: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(CategoryIcons.colorFor(item.category).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = CategoryIcons.iconFor(item.category),
                contentDescription = item.category.label,
                tint = CategoryIcons.colorFor(item.category),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.category.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = CategoryIcons.colorFor(item.category),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = formatCurrency(item.total),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ExpenseCard(
    expense: Expense,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(CategoryIcons.colorFor(expense.category).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = CategoryIcons.iconFor(expense.category),
                    contentDescription = expense.category.label,
                    tint = CategoryIcons.colorFor(expense.category),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = expense.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    if (expense.recurringId != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        AssistChip(
                            onClick = {},
                            label = { Text("Recurring", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
                Text(
                    text = "${expense.category.label} · ${formatDate(expense.date)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = formatCurrency(expense.amount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to delete \"${expense.title}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No expenses this month",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap + to add an expense",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
