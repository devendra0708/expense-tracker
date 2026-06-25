package com.expensetracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.expensetracker.app.ui.ExpenseTrackerNavHost
import com.expensetracker.app.ui.theme.ExpenseTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as ExpenseTrackerApp
        setContent {
            ExpenseTrackerTheme {
                ExpenseTrackerNavHost(app = app)
            }
        }
    }
}
