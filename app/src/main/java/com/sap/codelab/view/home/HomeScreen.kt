package com.sap.codelab.view.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sap.codelab.R
import com.sap.codelab.domain.model.Memo
import com.sap.codelab.mvi.CollectEffects
import com.sap.codelab.utils.permissions.rememberLocationPermissionController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    onOpenMemo: (Long) -> Unit,
    onCreateMemo: () -> Unit,
    model: HomeViewModel = hiltViewModel(),
) {
    val state by model.state.collectAsStateWithLifecycle()
    val permissionController = rememberLocationPermissionController()

    LaunchedEffect(Unit) { permissionController.ensurePermissions() }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) model.onIntent(HomeIntent.Refresh)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    CollectEffects(model.effects) { effect ->
        when (effect) {
            is HomeEffect.NavigateToMemo -> onOpenMemo(effect.memoId)
            HomeEffect.NavigateToCreateMemo -> onCreateMemo()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    if (state.showingAll) {
                        TextButton(onClick = { model.onIntent(HomeIntent.ShowOpen) }) {
                            Text(stringResource(R.string.action_show_open))
                        }
                    } else {
                        TextButton(onClick = { model.onIntent(HomeIntent.ShowAll) }) {
                            Text(stringResource(R.string.action_show_all))
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { model.onIntent(HomeIntent.CreateMemo) }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.new_memo))
            }
        },
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            items(state.memos, key = { it.id }) { memo ->
                MemoRow(
                    memo = memo,
                    onClick = { model.onIntent(HomeIntent.OpenMemo(memo.id)) },
                    onToggleDone = { isChecked -> model.onIntent(HomeIntent.ToggleDone(memo, isChecked)) },
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun MemoRow(
    memo: Memo,
    onClick: () -> Unit,
    onToggleDone: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = memo.isDone,
            enabled = !memo.isDone,
            onCheckedChange = onToggleDone,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = memo.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = memo.description,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
