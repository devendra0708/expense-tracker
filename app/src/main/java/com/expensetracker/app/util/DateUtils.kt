package com.expensetracker.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateUtils {
    fun currentYearMonth(): Pair<Int, Int> {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.YEAR) to (cal.get(Calendar.MONTH) + 1)
    }

    fun monthStartMillis(year: Int, month: Int): Long {
        return Calendar.getInstance().apply {
            set(year, month - 1, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun monthEndMillis(year: Int, month: Int): Long {
        return Calendar.getInstance().apply {
            set(year, month - 1, 1, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        }.timeInMillis
    }

    fun formatMonthYear(year: Int, month: Int): String {
        val cal = Calendar.getInstance().apply { set(year, month - 1, 1) }
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    fun formatMonthShort(year: Int, month: Int): String {
        val cal = Calendar.getInstance().apply { set(year, month - 1, 1) }
        return SimpleDateFormat("MMM yy", Locale.getDefault()).format(cal.time)
    }

    fun shiftMonth(year: Int, month: Int, delta: Int): Pair<Int, Int> {
        val cal = Calendar.getInstance().apply { set(year, month - 1, 1) }
        cal.add(Calendar.MONTH, delta)
        return cal.get(Calendar.YEAR) to (cal.get(Calendar.MONTH) + 1)
    }

    fun lastNMonths(n: Int): List<Pair<Int, Int>> {
        val (year, month) = currentYearMonth()
        return (n - 1 downTo 0).map { offset ->
            shiftMonth(year, month, -offset)
        }
    }

    fun endOfTodayMillis(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    fun calculateNextDueDate(currentDue: Long, frequency: com.expensetracker.app.data.RecurrenceFrequency): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = currentDue }
        when (frequency) {
            com.expensetracker.app.data.RecurrenceFrequency.WEEKLY ->
                cal.add(Calendar.WEEK_OF_YEAR, 1)
            com.expensetracker.app.data.RecurrenceFrequency.MONTHLY ->
                cal.add(Calendar.MONTH, 1)
            com.expensetracker.app.data.RecurrenceFrequency.YEARLY ->
                cal.add(Calendar.YEAR, 1)
        }
        return cal.timeInMillis
    }
}
