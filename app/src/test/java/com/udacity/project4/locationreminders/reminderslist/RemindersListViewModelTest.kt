package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects
    private lateinit var viewModel: RemindersListViewModel

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var reminderDataSource: ReminderDataSource

    @Before
    fun setupViewModel() {

        val reminder1 = ReminderDTO("reminder1", "desc1", "location1", 1.0, 1.0)
        val reminder2 = ReminderDTO("reminder2", "desc2", "location2", 2.0, 2.0)
        val reminder3 = ReminderDTO("reminder3", "desc3", "location3", 3.0, 3.0)
        val reminders :MutableList<ReminderDTO> = mutableListOf(reminder1, reminder2, reminder3)

        reminderDataSource = FakeDataSource(reminders)

        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), reminderDataSource)

    }

    @Test
    fun loadReminders_showData() {
        viewModel.loadReminders()

        val value = viewModel.showNoData.getOrAwaitValue()

        Assert.assertEquals(value, false)
    }

}