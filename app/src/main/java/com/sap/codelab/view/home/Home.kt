package com.sap.codelab.view.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityHomeBinding
import com.sap.codelab.domain.model.Memo
import com.sap.codelab.mvi.collectWhileStarted
import com.sap.codelab.utils.extensions.applyNavigationBarBottomMargin
import com.sap.codelab.utils.extensions.applyNavigationBarBottomPadding
import com.sap.codelab.utils.extensions.applyStatusBarTopPadding
import com.sap.codelab.utils.permissions.RuntimePermissionHelper
import com.sap.codelab.utils.permissions.locationReminderPermissions
import com.sap.codelab.view.create.CreateMemo
import com.sap.codelab.view.detail.BUNDLE_MEMO_ID
import com.sap.codelab.view.detail.ViewMemo
import dagger.hilt.android.AndroidEntryPoint

/**
 * The main activity of the app. Shows a list of recorded memos and lets the user add new memos.
 *
 * Follows MVI: the activity only sends [HomeIntent]s and renders [HomeState] / reacts to
 * [HomeEffect]. It holds no business logic of its own.
 */
@AndroidEntryPoint
internal class Home : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var model: HomeViewModel
    private lateinit var adapter: MemoAdapter
    private lateinit var menuItemShowAll: MenuItem
    private lateinit var menuItemShowOpen: MenuItem
    private val locationPermissionHelper = RuntimePermissionHelper(
        this,
        locationReminderPermissions(),
        rationaleTitle = R.string.permission_location_title,
        rationaleMessage = R.string.permission_location_rationale,
    )
    private val createMemoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            model.onIntent(HomeIntent.Refresh)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        applyWindowInsets()
        model = ViewModelProvider(this)[HomeViewModel::class.java]

        adapter = createAdapter()
        setupRecyclerView(adapter)
        binding.fab.setOnClickListener { model.onIntent(HomeIntent.CreateMemo) }

        observeState()
        observeEffects()
        model.onIntent(HomeIntent.ShowOpen)
        locationPermissionHelper.ensurePermissions()
    }

    /** Keeps the app bar, list and FAB clear of the edge-to-edge system bars. */
    private fun applyWindowInsets() {
        binding.appBar.applyStatusBarTopPadding()
        binding.contentHome.recyclerView.apply {
            clipToPadding = false
            applyNavigationBarBottomPadding()
        }
        binding.fab.applyNavigationBarBottomMargin()
    }

    /** Renders [HomeState]: the list contents and the open/all menu visibility. */
    private fun observeState() {
        collectWhileStarted(model.state) { state ->
            adapter.setItems(state.memos)
            if (::menuItemShowAll.isInitialized) {
                menuItemShowAll.isVisible = !state.showingAll
                menuItemShowOpen.isVisible = state.showingAll
            }
        }
    }

    /** Reacts to one-shot [HomeEffect] navigation events. */
    private fun observeEffects() {
        collectWhileStarted(model.effects) { effect ->
            when (effect) {
                is HomeEffect.NavigateToMemo -> showMemo(effect.memoId)
                HomeEffect.NavigateToCreateMemo ->
                    createMemoLauncher.launch(Intent(this, CreateMemo::class.java))
            }
        }
    }

    /**
     * Builds the adapter, translating user interactions into [HomeIntent]s.
     */
    private fun createAdapter(): MemoAdapter = MemoAdapter(mutableListOf(), { view ->
        model.onIntent(HomeIntent.OpenMemo((view.tag as Memo).id))
    }, { checkbox, isChecked ->
        model.onIntent(HomeIntent.ToggleDone(checkbox.tag as Memo, isChecked))
    })

    /**
     * Opens the Memo detail view for the given memoId.
     *
     * @param memoId    - the id of the memo to be shown.
     */
    private fun showMemo(memoId: Long) {
        val intent = Intent(this@Home, ViewMemo::class.java)
        intent.putExtra(BUNDLE_MEMO_ID, memoId)
        startActivity(intent)
    }

    /**
     * Initializes the recycler view to display the list of memos.
     */
    private fun setupRecyclerView(adapter: MemoAdapter) {
        binding.contentHome.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@Home, LinearLayoutManager.VERTICAL, false)
            this.adapter = adapter
            addItemDecoration(DividerItemDecoration(this@Home, (layoutManager as LinearLayoutManager).orientation))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        menuItemShowAll = menu.findItem(R.id.action_show_all)
        menuItemShowOpen = menu.findItem(R.id.action_show_open)
        // Reflect the current state in the freshly-inflated menu.
        val showingAll = model.state.value.showingAll
        menuItemShowAll.isVisible = !showingAll
        menuItemShowOpen.isVisible = showingAll
        return true
    }

    /**
     * Handles actionbar interactions by dispatching the corresponding intent.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_show_all -> {
                model.onIntent(HomeIntent.ShowAll)
                true
            }
            R.id.action_show_open -> {
                model.onIntent(HomeIntent.ShowOpen)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
