package com.sap.codelab.view.create

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sap.codelab.R
import com.sap.codelab.mvi.CollectEffects
import com.sap.codelab.utils.permissions.rememberLocationPermissionController
import com.sap.codelab.view.map.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateMemoScreen(
    pickedLocation: LatLng?,
    onLocationConsumed: () -> Unit,
    onPickLocation: (current: LatLng?) -> Unit,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    model: CreateMemoViewModel = hiltViewModel(),
) {
    val state by model.state.collectAsStateWithLifecycle()
    val permissionController = rememberLocationPermissionController()

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(pickedLocation) {
        pickedLocation?.let {
            model.onIntent(CreateMemoIntent.LocationPicked(it.latitude, it.longitude))
            onLocationConsumed()
        }
    }

    CollectEffects(model.effects) { effect ->
        when (effect) {
            is CreateMemoEffect.LaunchLocationPicker ->
                onPickLocation(
                    if (effect.latitude != null && effect.longitude != null) {
                        LatLng(effect.latitude, effect.longitude)
                    } else {
                        null
                    },
                )

            CreateMemoEffect.RequestPermissions -> permissionController.ensurePermissions()
            CreateMemoEffect.Saved -> onSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_memo)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.map_navigate_back),
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { model.onIntent(CreateMemoIntent.Save(title, description)) }) {
                        Text(stringResource(R.string.action_save))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.memo_title)) },
                isError = state.titleError,
                supportingText = if (state.titleError) {
                    { Text(stringResource(R.string.memo_title_empty_error)) }
                } else {
                    null
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.memo_text)) },
                isError = state.descriptionError,
                supportingText = if (state.descriptionError) {
                    { Text(stringResource(R.string.memo_text_empty_error)) }
                } else {
                    null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )
            Text(
                text = if (state.hasLocation) {
                    stringResource(R.string.memo_location_format, state.latitude!!, state.longitude!!)
                } else {
                    stringResource(R.string.memo_no_location)
                },
                modifier = Modifier.padding(top = 16.dp),
            )
            OutlinedButton(
                onClick = { model.onIntent(CreateMemoIntent.PickLocation) },
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(
                    stringResource(
                        if (state.hasLocation) R.string.memo_change_location else R.string.memo_pick_location,
                    ),
                )
            }
        }
    }
}
