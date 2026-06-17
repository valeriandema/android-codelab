package com.sap.codelab.view.create

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModelProvider
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityCreateMemoBinding
import com.sap.codelab.geofence.GeofenceManager
import com.sap.codelab.mvi.collectWhileStarted
import com.sap.codelab.utils.extensions.applyNavigationBarBottomPadding
import com.sap.codelab.utils.extensions.applyStatusBarTopPadding
import com.sap.codelab.utils.extensions.empty
import com.sap.codelab.utils.permissions.RuntimePermissionHelper
import com.sap.codelab.utils.permissions.locationReminderPermissions
import com.sap.codelab.view.map.LatLng
import com.sap.codelab.view.map.MapPickerActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Activity that allows a user to create a new Memo.
 *
 * Follows MVI: user actions become [CreateMemoIntent]s, the screen renders [CreateMemoState] and
 * reacts to [CreateMemoEffect]s (launching the picker, requesting permissions, finishing on save).
 */
@AndroidEntryPoint
internal class CreateMemo : AppCompatActivity() {

    private lateinit var binding: ActivityCreateMemoBinding
    private lateinit var model: CreateMemoViewModel
    private lateinit var permissionHelper: RuntimePermissionHelper
    private lateinit var pickLocation: ActivityResultLauncher<LatLng?>

    // Injected by Hilt; used to register a geofence once a located memo has been saved.
    @Inject
    lateinit var geofenceManager: GeofenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.appBar.applyStatusBarTopPadding()
        binding.contentCreateMemo.root.applyNavigationBarBottomPadding()
        model = ViewModelProvider(this)[CreateMemoViewModel::class.java]

        permissionHelper = RuntimePermissionHelper(
            this,
            locationReminderPermissions(),
            rationaleTitle = R.string.permission_location_title,
            rationaleMessage = R.string.permission_location_rationale,
        )
        pickLocation = registerForActivityResult(MapPickerActivity.PickLocation()) { latLng ->
            latLng?.let { model.onIntent(CreateMemoIntent.LocationPicked(it.latitude, it.longitude)) }
        }

        binding.contentCreateMemo.memoPickLocation.setOnClickListener {
            model.onIntent(CreateMemoIntent.PickLocation)
        }

        observeState()
        observeEffects()
    }

    /** Renders [CreateMemoState]: the location label/button and validation errors. */
    private fun observeState() {
        collectWhileStarted(model.state) { state ->
            binding.contentCreateMemo.run {
                if (state.hasLocation) {
                    memoLocation.text = getString(R.string.memo_location_format, state.latitude, state.longitude)
                    memoPickLocation.setText(R.string.memo_change_location)
                } else {
                    memoLocation.setText(R.string.memo_no_location)
                    memoPickLocation.setText(R.string.memo_pick_location)
                }
                memoTitleContainer.error = errorMessage(state.titleError, R.string.memo_title_empty_error)
                memoDescription.error = errorMessage(state.descriptionError, R.string.memo_text_empty_error)
            }
        }
    }

    /** Reacts to one-shot [CreateMemoEffect]s. */
    private fun observeEffects() {
        collectWhileStarted(model.effects) { effect ->
            when (effect) {
                is CreateMemoEffect.LaunchLocationPicker -> pickLocation.launch(effect.toLatLng())
                CreateMemoEffect.RequestPermissions -> permissionHelper.ensurePermissions()
                is CreateMemoEffect.MemoSaved -> {
                    if (effect.memo.hasLocation()) {
                        geofenceManager.addGeofence(effect.memo)
                    }
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_create_memo, menu)
        return true
    }

    /**
     * Handles actionbar interactions.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                binding.contentCreateMemo.run {
                    model.onIntent(
                        CreateMemoIntent.Save(memoTitle.text.toString(), memoDescription.text.toString())
                    )
                }
                true
            }

            else             -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Returns the error message if there is an error, or an empty string otherwise.
     *
     * @param hasError          - whether there is an error.
     * @param errorMessageResId - the resource id of the error message to show.
     * @return the error message if there is an error, or an empty string otherwise.
     */
    private fun errorMessage(hasError: Boolean, @StringRes errorMessageResId: Int): String {
        return if (hasError) {
            getString(errorMessageResId)
        } else {
            String.empty()
        }
    }
}

/** Builds the optional initial camera target for the picker from the effect payload. */
private fun CreateMemoEffect.LaunchLocationPicker.toLatLng(): LatLng? =
    if (latitude != null && longitude != null) LatLng(latitude, longitude) else null
