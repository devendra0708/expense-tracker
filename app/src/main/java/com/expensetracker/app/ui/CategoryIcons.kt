package com.expensetracker.app.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.expensetracker.app.data.ExpenseCategory

object CategoryIcons {
    fun iconFor(category: ExpenseCategory): ImageVector = when (category) {
        ExpenseCategory.FOOD -> Icons.Default.Restaurant
        ExpenseCategory.TRANSPORT -> Icons.Default.DirectionsCar
        ExpenseCategory.SHOPPING -> Icons.Default.ShoppingCart
        ExpenseCategory.BILLS -> Icons.Default.Receipt
        ExpenseCategory.ENTERTAINMENT -> Icons.Default.Movie
        ExpenseCategory.HEALTH -> Icons.Default.LocalHospital
        ExpenseCategory.OTHER -> Icons.Default.MoreHoriz
    }

    fun colorFor(category: ExpenseCategory): Color = when (category) {
        ExpenseCategory.FOOD -> Color(0xFFE65100)
        ExpenseCategory.TRANSPORT -> Color(0xFF1565C0)
        ExpenseCategory.SHOPPING -> Color(0xFF6A1B9A)
        ExpenseCategory.BILLS -> Color(0xFFC62828)
        ExpenseCategory.ENTERTAINMENT -> Color(0xFF2E7D32)
        ExpenseCategory.HEALTH -> Color(0xFF00838F)
        ExpenseCategory.OTHER -> Color(0xFF546E7A)
    }
}

fun formatCurrency(amount: Double): String = "$%.2f".format(amount)

fun formatDate(timestamp: Long): String {
    val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    return formatter.format(java.util.Date(timestamp))
}
