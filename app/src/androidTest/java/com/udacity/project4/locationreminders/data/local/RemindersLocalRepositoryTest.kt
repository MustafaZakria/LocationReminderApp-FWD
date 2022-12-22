package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.nullValue

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var localDataSource: RemindersLocalRepository
    private lateinit var database: RemindersDatabase


    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        // Using an in-memory database for testing, because it doesn't survive killing the process.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localDataSource =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    @After
    fun cleanUp() {
        database.close()
    }


    @Test
    fun saveTask_retrievesTask() = runTest {
        // GIVEN - A new reminder saved in the database.
        val reminder = ReminderDTO("title", "description", "location", 1.0, 1.0)
        database.reminderDao().saveReminder(reminder)

        // WHEN  - Task retrieved by ID.
        val result = localDataSource.getReminder(reminder.id)

        // THEN - Same task is returned.
        result as Result.Success
        MatcherAssert.assertThat(result.data.title, Is.`is`("title"))
        MatcherAssert.assertThat(result.data.description, Is.`is`("description"))
        MatcherAssert.assertThat(result.data.location, Is.`is`(reminder.location))
        MatcherAssert.assertThat(result.data.longitude, Is.`is`(reminder.longitude))
        MatcherAssert.assertThat(result.data.latitude, Is.`is`(reminder.latitude))
    }

    @Test
    fun getRemindersWhileEmpty_returnNoData() = runTest {
        // GIVEN - No reminders saved in the database.

        // WHEN  - Task retrieved by ID.
        val result = localDataSource.getReminders()

        result as Result.Success
        MatcherAssert.assertThat(result.data, Is.`is`(emptyList()))
    }
}