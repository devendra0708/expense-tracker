package com.expensetracker.app

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExpenseTrackerFunctionalTest {
    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var device: UiDevice

    @Before
    fun resetLocalStorage() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        device = UiDevice.getInstance(instrumentation)
    }

    @After
    fun closeActivity() {
        if (::scenario.isInitialized) {
            scenario.close()
        }
    }

    @Test
    fun profileDetailsAndCurrencyPersistAcrossRelaunch() {
        val suffix = System.currentTimeMillis()
        val name = "Avery$suffix"
        val email = "avery$suffix"
        val expenseTitle = "EuroProbe$suffix"

        launchApp()
        openProfile()

        setText("Profile name", name)
        setText("Profile email", email)
        waitForDescription("Currency EUR").click()
        waitForText("Save Settings").click()

        assertTextVisible(name)
        assertTextVisible(email)

        scenario.close()
        launchApp()
        openProfile()

        assertTextVisible(name)
        assertTextVisible(email)

        openHome()
        waitForDescription("Add expense").click()
        assertTextVisible("Add Expense")
        setText("Expense title", expenseTitle)
        setText("Expense amount", "1.00")
        waitForText("Save Expense").click()

        assertTextVisible(expenseTitle)
        assertTextVisible("€1.00")
    }

    @Test
    fun addingExpenseShowsItInMonthlyListAndTotals() {
        val expenseTitle = "Coffee${System.currentTimeMillis()}"

        launchApp()
        setCurrency("USD")
        openHome()

        waitForDescription("Add expense").click()
        setText("Expense title", expenseTitle)
        setText("Expense amount", "12.50")
        waitForText("Save Expense").click()

        assertTextVisible(expenseTitle)
        assertTextVisible("\$12.50")
    }

    @Test
    fun addingRecurringExpenseUpdatesRecurringHomeAndCharts() {
        val recurringTitle = "Subscription${System.currentTimeMillis()}"

        launchApp()
        setCurrency("GBP")
        openRecurring()

        waitForText("Add").click()
        setText("Recurring title", recurringTitle)
        setText("Recurring amount", "15.99")
        waitForText("Save Recurring").click()

        assertTextVisible(recurringTitle)
        assertTextVisible("£15.99")

        openHome()
        assertTextVisible(recurringTitle)
        assertTextVisible("£15.99")

        openCharts()
        assertTextVisible("Spending This Month")
    }

    private fun launchApp() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        assertTextVisible("Expense Tracker")
    }

    private fun openHome() {
        waitForText("Home").click()
        device.waitForIdle()
    }

    private fun openProfile() {
        waitForText("Profile").click()
        device.waitForIdle()
    }

    private fun openRecurring() {
        waitForText("Recurring").click()
        device.waitForIdle()
    }

    private fun openCharts() {
        waitForText("Charts").click()
        device.waitForIdle()
    }

    private fun setCurrency(code: String) {
        openProfile()
        waitForDescription("Currency $code").click()
        waitForText("Save Settings").click()
    }

    private fun setText(description: String, value: String) {
        val field = waitForDescription(description)
        field.click()
        device.executeShellCommand("input text $value")
        device.waitForIdle()
    }

    private fun assertTextVisible(text: String) {
        assertNotNull("Expected to find text: $text", waitForText(text))
    }

    private fun waitForText(text: String): UiObject2 = waitForObject(By.text(text))

    private fun waitForDescription(description: String): UiObject2 = waitForObject(By.desc(description))

    private fun waitForObject(selector: BySelector): UiObject2 {
        repeat(6) {
            device.wait(Until.findObject(selector), 1_000)?.let { return it }
            scrollDown()
        }
        error("Unable to find UI object for selector: $selector")
    }

    private fun scrollDown() {
        val width = device.displayWidth
        val height = device.displayHeight
        device.swipe(width / 2, height * 3 / 4, width / 2, height / 4, 20)
        device.waitForIdle()
    }
}
