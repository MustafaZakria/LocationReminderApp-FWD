package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var viewModel: RemindersListViewModel

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var reminderDataSource: FakeDataSource

    @Before
    fun setupViewModel() {

        val reminder1 = ReminderDTO("reminder1", "desc1", "location1", 1.0, 1.0)
        val reminder2 = ReminderDTO("reminder2", "desc2", "location2", 2.0, 2.0)
        val reminder3 = ReminderDTO("reminder3", "desc3", "location3", 3.0, 3.0)
        val reminders :MutableList<ReminderDTO> = mutableListOf(reminder1, reminder2, reminder3)

        reminderDataSource = FakeDataSource(reminders)

        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), reminderDataSource)

    }

    @After
    fun cleanUp() {
        stopKoin()
    }

    @Test
    fun loadReminders_showData() {
        viewModel.loadReminders()

        val value = viewModel.showNoData.getOrAwaitValue()

        Assert.assertEquals(value, false)
    }

    @Test
    fun loadRemindersWhenRemindersAreUnavailable_callErrorToDisplay() {
        // Make the repository return errors.
        reminderDataSource.setReturnError(true)
        viewModel.loadReminders()

        // Then empty and error are true (which triggers an error message to be shown).
        MatcherAssert.assertThat(viewModel.empty.getOrAwaitValue(), Is.`is`(true))
        MatcherAssert.assertThat(viewModel.error.getOrAwaitValue(), Is.`is`(true))
    }

    @Test
    fun loadReminders_loading() {
        // Pause dispatcher so you can verify initial values.
        mainCoroutineRule.pauseDispatcher()

        // Load the task in the view model.
        viewModel.loadReminders()

        // Then assert that the progress indicator is shown.
        MatcherAssert.assertThat(viewModel.showLoading.getOrAwaitValue(), Is.`is`(true))

        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()

        // Then assert that the progress indicator is hidden.
        MatcherAssert.assertThat(viewModel.showLoading.getOrAwaitValue(), Is.`is`(false))
    }

}