package com.expensetracker.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.expensetracker.app.data.ExpenseCategory
import com.expensetracker.app.data.RecurrenceFrequency
import com.expensetracker.app.ui.formatDate
import com.expensetracker.app.viewmodel.ExpenseViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecurringScreen(
    viewModel: ExpenseViewModel,
    recurringId: Long?,
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ExpenseCategory.BILLS) }
    var frequency by remember { mutableStateOf(RecurrenceFrequency.MONTHLY) }
    var note by remember { mutableStateOf("") }
    var startDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var isActive by remember { mutableStateOf(true) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showFrequencyMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var loaded by remember(recurringId) { mutableStateOf(recurringId == null) }
    val userSettings by viewModel.userSettings.collectAsState()
    val currencySymbol = userSettings.currency.symbol

    LaunchedEffect(recurringId) {
        if (recurringId != null && recurringId > 0) {
            val recurring = viewModel.getRecurringById(recurringId) ?: return@LaunchedEffect
            title = recurring.title
            amountText = recurring.amount.toString()
            category = recurring.category
            frequency = recurring.frequency
            note = recurring.note
            startDateMillis = recurring.startDate
            isActive = recurring.isActive
            loaded = true
        }
    }

    val isEditing = recurringId != null && recurringId > 0
    val amount = amountText.toDoubleOrNull()
    val isValid = title.isNotBlank() && amount != null && amount > 0

    if (isEditing && !loaded) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Recurring" else "Add Recurring") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recurring_title_input")
                    .semantics { contentDescription = "Recurring title" },
                singleLine = true,
                placeholder = { Text("e.g. Netflix, Rent") }
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        amountText = input
                    }
                },
                label = { Text("Amount") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recurring_amount_input")
                    .semantics { contentDescription = "Recurring amount" },
                singleLine = true,
                prefix = { Text(currencySymbol) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            ExposedDropdownMenuBox(
                expanded = showCategoryMenu,
                onExpandedChange = { showCategoryMenu = it }
            ) {
                OutlinedTextField(
                    value = category.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) }
                )
                ExposedDropdownMenu(
                    expanded = showCategoryMenu,
                    onDismissRequest = { showCategoryMenu = false }
                ) {
                    ExpenseCategory.entries.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.label) },
                            onClick = {
                                category = cat
                                showCategoryMenu = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = showFrequencyMenu,
                onExpandedChange = { showFrequencyMenu = it }
            ) {
                OutlinedTextField(
                    value = frequency.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Frequency") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showFrequencyMenu) }
                )
                ExposedDropdownMenu(
                    expanded = showFrequencyMenu,
                    onDismissRequest = { showFrequencyMenu = false }
                ) {
                    RecurrenceFrequency.entries.forEach { freq ->
                        DropdownMenuItem(
                            text = { Text(freq.label) },
                            onClick = {
                                frequency = freq
                                showFrequencyMenu = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = formatDate(startDateMillis),
                onValueChange = {},
                readOnly = true,
                label = { Text("Start date") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    TextButton(onClick = { showDatePicker = true }) {
                        Text("Change")
                    }
                }
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            if (isEditing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Active", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
            }

            Button(
                onClick = {
                    viewModel.saveRecurring(
                        id = recurringId ?: 0L,
                        title = title,
                        amount = amount!!,
                        category = category,
                        frequency = frequency,
                        startDate = startDateMillis,
                        note = note,
                        isActive = isActive
                    )
                    onNavigateBack()
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_recurring_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isEditing) "Update Recurring" else "Save Recurring",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDateMillis,
            yearRange = IntRange(2020, Calendar.getInstance().get(Calendar.YEAR) + 5)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { startDateMillis = it }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
