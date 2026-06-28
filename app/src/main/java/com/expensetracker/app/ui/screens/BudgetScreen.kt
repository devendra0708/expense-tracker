package com.expensetracker.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.expensetracker.app.data.ExpenseCategory
import com.expensetracker.app.ui.CategoryIcons
import com.expensetracker.app.ui.components.BudgetProgressBar
import com.expensetracker.app.ui.formatCurrency
import com.expensetracker.app.util.DateUtils
import com.expensetracker.app.viewmodel.ExpenseViewModel

@Composable
fun BudgetScreen(viewModel: ExpenseViewModel) {
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val budgetStatus by viewModel.budgetStatus.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val (year, month) = selectedMonth
    val currencySymbol = userSettings.currency.symbol

    var totalBudgetText by remember(year, month, budgetStatus.monthlyBudget) {
        mutableStateOf(budgetStatus.monthlyBudget?.toString() ?: "")
    }
    var categoryBudgetTexts by remember(year, month) {
        mutableStateOf(ExpenseCategory.entries.associateWith { "" })
    }

    LaunchedEffect(year, month, budgetStatus.categoryBudgets) {
        categoryBudgetTexts = ExpenseCategory.entries.associateWith { category ->
            budgetStatus.categoryBudgets[category]?.toString() ?: ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MonthSelector(
            label = DateUtils.formatMonthYear(year, month),
            onPrevious = { viewModel.shiftSelectedMonth(-1) },
            onNext = { viewModel.shiftSelectedMonth(1) }
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Monthly Budget",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                val monthlyBudget = budgetStatus.monthlyBudget
                if (monthlyBudget != null && monthlyBudget > 0) {
                    BudgetProgressBar(
                        spent = budgetStatus.monthlySpent,
                        budget = monthlyBudget,
                        currencySymbol = currencySymbol
                    )
                } else {
                    Text(
                        text = "Spent this month: ${formatCurrency(budgetStatus.monthlySpent, currencySymbol)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Set a budget below to track progress",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Set Total Budget",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = totalBudgetText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            totalBudgetText = input
                        }
                    },
                    label = { Text("Monthly limit") },
                    prefix = { Text(currencySymbol) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Button(
                    onClick = {
                        totalBudgetText.toDoubleOrNull()?.let { viewModel.saveMonthlyBudget(it) }
                    },
                    enabled = totalBudgetText.toDoubleOrNull()?.let { it > 0 } == true,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Total Budget")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Category Budgets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Optional limits per category",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                ExpenseCategory.entries.forEach { category ->
                    val spent = budgetStatus.categorySpent[category] ?: 0.0
                    val budget = budgetStatus.categoryBudgets[category]
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = category.label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (budget != null) {
                                    "${formatCurrency(spent, currencySymbol)} / ${formatCurrency(budget, currencySymbol)}"
                                } else {
                                    formatCurrency(spent, currencySymbol)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (budget != null && spent > budget) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        OutlinedTextField(
                            value = categoryBudgetTexts[category] ?: "",
                            onValueChange = { input ->
                                if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                    categoryBudgetTexts = categoryBudgetTexts.toMutableMap().apply {
                                        put(category, input)
                                    }
                                }
                            },
                            label = { Text("Budget for ${category.label}") },
                            prefix = { Text(currencySymbol) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            leadingIcon = {
                                Icon(
                                    CategoryIcons.iconFor(category),
                                    contentDescription = null,
                                    tint = CategoryIcons.colorFor(category)
                                )
                            }
                        )
                    }
                }

                Button(
                    onClick = {
                        val budgets = categoryBudgetTexts.mapNotNull { (category, text) ->
                            text.toDoubleOrNull()?.takeIf { it > 0 }?.let { category to it }
                        }.toMap()
                        viewModel.saveCategoryBudgets(budgets)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Category Budgets")
                }
            }
        }
    }
}

@Composable
fun MonthSelector(
    label: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous month")
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next month")
        }
    }
}
