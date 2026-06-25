package com.expensetracker.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.expensetracker.app.ExpenseTrackerApp
import com.expensetracker.app.ui.screens.*
import com.expensetracker.app.viewmodel.ExpenseViewModel
import com.expensetracker.app.viewmodel.ExpenseViewModelFactory

object Routes {
    const val HOME = "home"
    const val CHARTS = "charts"
    const val BUDGET = "budget"
    const val RECURRING = "recurring"
    const val ADD_EXPENSE = "add_expense"
    const val EDIT_EXPENSE = "edit_expense/{expenseId}"
    const val ADD_RECURRING = "add_recurring"
    const val EDIT_RECURRING = "edit_recurring/{recurringId}"

    fun editExpense(expenseId: Long) = "edit_expense/$expenseId"
    fun editRecurring(recurringId: Long) = "edit_recurring/$recurringId"
}

private enum class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    HOME(Routes.HOME, "Home", Icons.Default.Home),
    CHARTS(Routes.CHARTS, "Charts", Icons.Default.BarChart),
    BUDGET(Routes.BUDGET, "Budget", Icons.Default.AccountBalanceWallet),
    RECURRING(Routes.RECURRING, "Recurring", Icons.Default.Repeat)
}

@Composable
fun ExpenseTrackerNavHost(app: ExpenseTrackerApp) {
    val navController = rememberNavController()
    val viewModel: ExpenseViewModel = viewModel(
        factory = ExpenseViewModelFactory(app.repository)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = BottomTab.entries.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    BottomTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    viewModel = viewModel,
                    onAddExpense = { navController.navigate(Routes.ADD_EXPENSE) },
                    onEditExpense = { id -> navController.navigate(Routes.editExpense(id)) }
                )
            }
            composable(Routes.CHARTS) {
                ChartsScreen(viewModel = viewModel)
            }
            composable(Routes.BUDGET) {
                BudgetScreen(viewModel = viewModel)
            }
            composable(Routes.RECURRING) {
                RecurringScreen(
                    viewModel = viewModel,
                    onAddRecurring = { navController.navigate(Routes.ADD_RECURRING) },
                    onEditRecurring = { id -> navController.navigate(Routes.editRecurring(id)) }
                )
            }
            composable(Routes.ADD_EXPENSE) {
                AddEditExpenseScreen(
                    viewModel = viewModel,
                    expenseId = null,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Routes.EDIT_EXPENSE,
                arguments = listOf(navArgument("expenseId") { type = NavType.LongType })
            ) { backStackEntry ->
                val expenseId = backStackEntry.arguments?.getLong("expenseId")
                AddEditExpenseScreen(
                    viewModel = viewModel,
                    expenseId = expenseId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Routes.ADD_RECURRING) {
                AddEditRecurringScreen(
                    viewModel = viewModel,
                    recurringId = null,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Routes.EDIT_RECURRING,
                arguments = listOf(navArgument("recurringId") { type = NavType.LongType })
            ) { backStackEntry ->
                val recurringId = backStackEntry.arguments?.getLong("recurringId")
                AddEditRecurringScreen(
                    viewModel = viewModel,
                    recurringId = recurringId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
