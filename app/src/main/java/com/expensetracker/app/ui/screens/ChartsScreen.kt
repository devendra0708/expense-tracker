package com.expensetracker.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.expensetracker.app.ui.components.CategoryPieChart
import com.expensetracker.app.ui.components.MonthlyBarChart
import com.expensetracker.app.ui.formatCurrency
import com.expensetracker.app.util.DateUtils
import com.expensetracker.app.viewmodel.ExpenseViewModel

@Composable
fun ChartsScreen(viewModel: ExpenseViewModel) {
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val monthlyCategoryTotals by viewModel.monthlyCategoryTotals.collectAsState()
    val monthlySpent by viewModel.monthlySpent.collectAsState()
    val monthlyChartData by viewModel.monthlyChartData.collectAsState()
    val (year, month) = selectedMonth

    LaunchedEffect(Unit) {
        viewModel.refreshMonthlyChartData()
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

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Spending This Month",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatCurrency(monthlySpent),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Category Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                CategoryPieChart(categoryTotals = monthlyCategoryTotals)
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Last 6 Months",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                MonthlyBarChart(monthlyTotals = monthlyChartData)
            }
        }
    }
}
