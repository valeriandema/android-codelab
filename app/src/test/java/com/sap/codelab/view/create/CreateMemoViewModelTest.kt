package com.sap.codelab.view.create

import app.cash.turbine.test
import com.sap.codelab.FakeMemoRepository
import com.sap.codelab.MainDispatcherRule
import com.sap.codelab.domain.model.Memo
import com.sap.codelab.domain.usecase.SaveMemoUseCase
import com.sap.codelab.geofence.GeofenceManager
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CreateMemoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeMemoRepository
    private lateinit var geofenceManager: GeofenceManager
    private lateinit var viewModel: CreateMemoViewModel

    @Before
    fun setUp() {
        repository = FakeMemoRepository()
        geofenceManager = mockk(relaxed = true)
        viewModel = CreateMemoViewModel(
            saveMemo = SaveMemoUseCase(repository),
            geofenceManager = geofenceManager,
            ioDispatcher = mainDispatcherRule.dispatcher,
        )
    }

    @Test
    fun `PickLocation emits the picker effect carrying the current location`() =
        runTest(mainDispatcherRule.dispatcher) {
            viewModel.effects.test {
                viewModel.onIntent(CreateMemoIntent.PickLocation)

                assertEquals(CreateMemoEffect.LaunchLocationPicker(null, null), awaitItem())
            }
        }

    @Test
    fun `LocationPicked stores the location and requests permissions`() =
        runTest(mainDispatcherRule.dispatcher) {
            viewModel.state.test {
                viewModel.effects.test {
                    viewModel.onIntent(CreateMemoIntent.LocationPicked(48.1, 11.6))

                    assertEquals(CreateMemoEffect.RequestPermissions, awaitItem())
                }
                val state = expectMostRecentItem()
                assertEquals(48.1, state.latitude!!, 0.0)
                assertEquals(11.6, state.longitude!!, 0.0)
                assertTrue(state.hasLocation)
            }
        }

    @Test
    fun `PickLocation after a location was picked carries that location`() =
        runTest(mainDispatcherRule.dispatcher) {
            viewModel.effects.test {
                viewModel.onIntent(CreateMemoIntent.LocationPicked(48.1, 11.6))
                assertEquals(CreateMemoEffect.RequestPermissions, awaitItem())

                viewModel.onIntent(CreateMemoIntent.PickLocation)
                assertEquals(CreateMemoEffect.LaunchLocationPicker(48.1, 11.6), awaitItem())
            }
        }

    @Test
    fun `Save with a blank title sets the title error and does not persist`() =
        runTest(mainDispatcherRule.dispatcher) {
            viewModel.state.test {
                viewModel.onIntent(CreateMemoIntent.Save(title = "  ", description = "desc"))

                val state = expectMostRecentItem()
                assertTrue(state.titleError)
                assertFalse(state.descriptionError)
            }
            assertTrue(repository.saved.isEmpty())
            verify(exactly = 0) { geofenceManager.addGeofence(any()) }
        }

    @Test
    fun `Save with a blank description sets the description error`() =
        runTest(mainDispatcherRule.dispatcher) {
            viewModel.state.test {
                viewModel.onIntent(CreateMemoIntent.Save(title = "title", description = ""))

                val state = expectMostRecentItem()
                assertFalse(state.titleError)
                assertTrue(state.descriptionError)
            }
            assertTrue(repository.saved.isEmpty())
        }

    @Test
    fun `Save without a location persists, emits Saved and registers no geofence`() =
        runTest(mainDispatcherRule.dispatcher) {
            viewModel.effects.test {
                viewModel.onIntent(CreateMemoIntent.Save(title = "Buy milk", description = "2%"))

                assertEquals(CreateMemoEffect.Saved, awaitItem())
            }
            assertEquals(1, repository.saved.size)
            verify(exactly = 0) { geofenceManager.addGeofence(any()) }
            confirmVerified(geofenceManager)
        }

    @Test
    fun `Save with a location registers a geofence for the saved memo and emits Saved`() =
        runTest(mainDispatcherRule.dispatcher) {
            repository.nextId = 77

            viewModel.effects.test {
                viewModel.onIntent(CreateMemoIntent.LocationPicked(48.1, 11.6))
                assertEquals(CreateMemoEffect.RequestPermissions, awaitItem())

                viewModel.onIntent(CreateMemoIntent.Save(title = "At home", description = "do laundry"))
                assertEquals(CreateMemoEffect.Saved, awaitItem())
            }
            assertEquals(1, repository.saved.size)
            verify(exactly = 1) {
                geofenceManager.addGeofence(match<Memo> { it.id == 77L && it.hasLocation() })
            }
        }
}
