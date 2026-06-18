package com.sap.codelab.view.map

import app.cash.turbine.test
import com.sap.codelab.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MapPickerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: MapPickerViewModel

    @Before
    fun setUp() {
        viewModel = MapPickerViewModel()
    }

    @Test
    fun `Initialize seeds the selected point`() = runTest(mainDispatcherRule.dispatcher) {
        viewModel.state.test {
            viewModel.onIntent(MapPickerIntent.Initialize(LatLng(1.0, 2.0)))

            assertEquals(LatLng(1.0, 2.0), expectMostRecentItem().selected)
        }
    }

    @Test
    fun `Initialize with null leaves nothing selected`() = runTest(mainDispatcherRule.dispatcher) {
        viewModel.state.test {
            viewModel.onIntent(MapPickerIntent.Initialize(null))

            assertNull(expectMostRecentItem().selected)
        }
    }

    @Test
    fun `MapClicked updates the selected point`() = runTest(mainDispatcherRule.dispatcher) {
        viewModel.state.test {
            viewModel.onIntent(MapPickerIntent.MapClicked(LatLng(3.0, 4.0)))

            assertEquals(LatLng(3.0, 4.0), expectMostRecentItem().selected)
        }
    }

    @Test
    fun `Confirm with a selection emits Finish with the picked point`() =
        runTest(mainDispatcherRule.dispatcher) {
            viewModel.onIntent(MapPickerIntent.MapClicked(LatLng(5.0, 6.0)))

            viewModel.effects.test {
                viewModel.onIntent(MapPickerIntent.Confirm)

                assertEquals(MapPickerEffect.Finish(LatLng(5.0, 6.0)), awaitItem())
            }
        }

    @Test
    fun `Confirm without a selection emits nothing`() = runTest(mainDispatcherRule.dispatcher) {
        viewModel.effects.test {
            viewModel.onIntent(MapPickerIntent.Confirm)

            expectNoEvents()
        }
    }
}
