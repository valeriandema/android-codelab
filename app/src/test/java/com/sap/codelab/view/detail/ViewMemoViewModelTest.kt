package com.sap.codelab.view.detail

import app.cash.turbine.test
import com.sap.codelab.FakeMemoRepository
import com.sap.codelab.MainDispatcherRule
import com.sap.codelab.memo
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ViewMemoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeMemoRepository
    private lateinit var viewModel: ViewMemoViewModel

    @Before
    fun setUp() {
        repository = FakeMemoRepository()
        viewModel = ViewMemoViewModel(repository, mainDispatcherRule.dispatcher)
    }

    @Test
    fun `initial state has no memo`() = runTest(mainDispatcherRule.dispatcher) {
        viewModel.state.test {
            assertNull(awaitItem().memo)
        }
    }

    @Test
    fun `Load fetches the memo by id and puts it in state`() = runTest(mainDispatcherRule.dispatcher) {
        repository.memoById = memo(id = 5, title = "Loaded")

        viewModel.state.test {
            viewModel.onIntent(ViewMemoIntent.Load(memoId = 5))

            val memo = expectMostRecentItem().memo
            assertEquals(5L, memo?.id)
            assertEquals("Loaded", memo?.title)
        }
    }
}
