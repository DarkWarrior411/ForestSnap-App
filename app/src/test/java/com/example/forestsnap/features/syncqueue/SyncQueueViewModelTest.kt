package com.example.forestsnap.features.syncqueue

import app.cash.turbine.test
import com.example.forestsnap.data.local.SyncSnapEntity
import com.example.forestsnap.data.repository.SyncSnapRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SyncQueueViewModelTest {

    private lateinit var viewModel: SyncQueueViewModel
    private val repository = mockk<SyncSnapRepository>()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `pendingQueue exposes data from repository`() = runTest {
        val mockData = listOf(
            SyncSnapEntity(
                id = 1,
                photoPath = "path1",
                latitude = 0.0,
                longitude = 0.0,
                timestamp = 0L
            ),
            SyncSnapEntity(
                id = 2,
                photoPath = "path2",
                latitude = 0.0,
                longitude = 0.0,
                timestamp = 0L
            )
        )

        every { repository.getUnsynced() } returns flowOf(mockData)

        viewModel = SyncQueueViewModel(repository)

        viewModel.pendingQueue.test {
            assertEquals(emptyList<SyncSnapEntity>(), awaitItem())
            assertEquals(mockData, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
