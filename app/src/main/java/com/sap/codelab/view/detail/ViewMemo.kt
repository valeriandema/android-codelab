package com.sap.codelab.view.detail

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityViewMemoBinding
import com.sap.codelab.domain.model.Memo
import com.sap.codelab.mvi.collectWhileStarted
import com.sap.codelab.utils.extensions.applyNavigationBarBottomPadding
import com.sap.codelab.utils.extensions.applyStatusBarTopPadding
import dagger.hilt.android.AndroidEntryPoint

internal const val BUNDLE_MEMO_ID: String = "memoId"

/**
 * Activity that allows a user to see the details of a memo.
 *
 * Follows MVI: it sends a [ViewMemoIntent.Load] and renders the resulting [ViewMemoState].
 */
@AndroidEntryPoint
internal class ViewMemo : AppCompatActivity() {

    private lateinit var binding: ActivityViewMemoBinding
    private lateinit var model: ViewMemoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.appBar.applyStatusBarTopPadding()
        binding.contentCreateMemo.root.applyNavigationBarBottomPadding()
        model = ViewModelProvider(this)[ViewMemoViewModel::class.java]

        collectWhileStarted(model.state) { state ->
            state.memo?.let { updateUI(it) }
        }

        if (savedInstanceState == null) {
            model.onIntent(ViewMemoIntent.Load(intent.getLongExtra(BUNDLE_MEMO_ID, -1)))
        }
    }

    /**
     * Updates the UI with the given memo details.
     *
     * @param memo - the memo whose details are to be displayed.
     */
    private fun updateUI(memo: Memo) {
        binding.contentCreateMemo.run {
            memoTitle.setText(memo.title)
            memoDescription.setText(memo.description)
            memoTitle.isEnabled = false
            memoDescription.isEnabled = false
            // The memo detail screen is read-only, so hide the picker and show the location as text.
            memoPickLocation.visibility = View.GONE
            memoLocation.text = if (memo.hasLocation()) {
                getString(R.string.memo_location_format, memo.reminderLatitude, memo.reminderLongitude)
            } else {
                getString(R.string.memo_no_location)
            }
        }
    }
}
