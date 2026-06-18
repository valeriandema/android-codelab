package com.sap.codelab.view.home

import app.cash.turbine.test
import com.sap.codelab.FakeMemoRepository
import com.sap.codelab.MainDispatcherRule
import com.sap.codelab.memo
import com.sap.codelab.domain.usecase.MarkMemoDoneUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeMemoRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        repository = FakeMemoRepository()
        viewModel = HomeViewModel(repository, MarkMemoDoneUseCase(repository), mainDispatcherRule.dispatcher)
    }

    @Test
    fun `ShowOpen loads open memos`() = runTest(mainDispatcherRule.dispatcher) {
        repository.openMemos = listOf(memo(1))
        repository.allMemos = listOf(memo(1), memo(2))

        viewModel.state.test {
            viewModel.onIntent(HomeIntent.ShowOpen)

            val state = expectMostRecentItem()
            assertEquals(listOf(1L), state.memos.map { it.id })
            assertFalse(state.showingAll)
        }
    }

    @Test
    fun `ShowAll loads all memos`() = runTest(mainDispatcherRule.dispatcher) {
        repository.openMemos = listOf(memo(1))
        repository.allMemos = listOf(memo(1), memo(2))

        viewModel.state.test {
            viewModel.onIntent(HomeIntent.ShowAll)

            val state = expectMostRecentItem()
            assertEquals(listOf(1L, 2L), state.memos.map { it.id })
            assertTrue(state.showingAll)
        }
    }

    @Test
    fun `Refresh keeps the current all filter`() = runTest(mainDispatcherRule.dispatcher) {
        repository.allMemos = listOf(memo(1), memo(2))
        viewModel.onIntent(HomeIntent.ShowAll)

        viewModel.state.test {
            repository.allMemos = listOf(memo(1), memo(2), memo(3))
            viewModel.onIntent(HomeIntent.Refresh)

            val state = expectMostRecentItem()
            assertTrue(state.showingAll)
            assertEquals(3, state.memos.size)
        }
    }

    @Test
    fun `Refresh keeps the current open filter`() = runTest(mainDispatcherRule.dispatcher) {
        repository.openMemos = listOf(memo(1))
        viewModel.onIntent(HomeIntent.ShowOpen)

        viewModel.state.test {
            repository.openMemos = listOf(memo(1), memo(2))
            viewModel.onIntent(HomeIntent.Refresh)

            val state = expectMostRecentItem()
            assertFalse(state.showingAll)
            assertEquals(2, state.memos.size)
        }
    }

    @Test
    fun `ToggleDone marks the memo done and reloads under the current filter`() =
        runTest(mainDispatcherRule.dispatcher) {
            repository.openMemos = listOf(memo(1), memo(2))
            viewModel.onIntent(HomeIntent.ShowOpen)

            viewModel.state.test {
                repository.openMemos = listOf(memo(2))
                viewModel.onIntent(HomeIntent.ToggleDone(memo(1), isDone = true))

                val state = expectMostRecentItem()
                assertEquals(listOf(2L), state.memos.map { it.id })
            }
            assertTrue(repository.saved.single().isDone)
            assertEquals(1L, repository.saved.single().id)
        }

    @Test
    fun `ToggleDone with isDone false is a no-op`() {
        viewModel.onIntent(HomeIntent.ToggleDone(memo(1), isDone = false))

        assertTrue(repository.saved.isEmpty())
    }

    @Test
    fun `OpenMemo emits a navigate-to-memo effect`() = runTest(mainDispatcherRule.dispatcher) {
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.OpenMemo(memoId = 9))

            assertEquals(HomeEffect.NavigateToMemo(9), awaitItem())
        }
    }

    @Test
    fun `CreateMemo emits a navigate-to-create effect`() = runTest(mainDispatcherRule.dispatcher) {
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.CreateMemo)

            assertEquals(HomeEffect.NavigateToCreateMemo, awaitItem())
        }
    }
}
