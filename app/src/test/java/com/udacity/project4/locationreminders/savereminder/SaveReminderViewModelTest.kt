package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {


    //TODO: provide testing to the SaveReminderView and its live data objects
    private lateinit var viewModel: SaveReminderViewModel

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

        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), reminderDataSource)

    }

    @After
    fun cleanUp()  {
        stopKoin()
    }

    @Test
    fun validateEnteredData_invalidTitle_showSnackBarTitleError() {
        val reminder = ReminderDataItem(null, "desc", "location", 1.0, 1.0)
        viewModel.validateEnteredData(reminder)

        val value = viewModel.showSnackBarInt.getOrAwaitValue()

        Assert.assertEquals(value, R.string.err_enter_title)
    }

    @Test
    fun validateEnteredData_invalidLocation_showSnackBarTitleError() {
        val reminder = ReminderDataItem("title", "desc", null, 1.0, 1.0)
        viewModel.validateEnteredData(reminder)

        val value = viewModel.showSnackBarInt.getOrAwaitValue()

        Assert.assertEquals(value, R.string.err_select_location)
    }

    @Test
    fun onClear_setsReminderTitleNull() {
        viewModel.onClear()

        val title = viewModel.reminderTitle.getOrAwaitValue()

        Assert.assertEquals(title, null)
    }


    @Test
    fun saveReminder_setsShowToast() {
        val reminder = ReminderDataItem("title", "desc", "location", 1.0, 1.0)

        viewModel.saveReminder(reminder)

        val value = viewModel.showToast.getOrAwaitValue()

        Assert.assertThat(
            value,
            CoreMatchers.not(CoreMatchers.nullValue())
        )
    }

    @Test
    fun saveReminder_navigateBack() {
        val reminder = ReminderDataItem("title", "desc", "location", 1.0, 1.0)

        viewModel.saveReminder(reminder)

        val value = viewModel.navigationCommand.getOrAwaitValue()

        Assert.assertEquals(value, NavigationCommand.Back)
    }
}