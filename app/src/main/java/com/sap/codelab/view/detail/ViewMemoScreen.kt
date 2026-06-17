package com.sap.codelab.view.detail

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sap.codelab.R
import com.sap.codelab.domain.model.Memo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ViewMemoScreen(
    memoId: Long,
    onBack: () -> Unit,
    model: ViewMemoViewModel = hiltViewModel(),
) {
    val state by model.state.collectAsStateWithLifecycle()

    LaunchedEffect(memoId) { model.onIntent(ViewMemoIntent.Load(memoId)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.map_navigate_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        state.memo?.let { memo ->
            MemoDetail(memo = memo, modifier = Modifier.padding(padding))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemoDetail(memo: Memo, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        OutlinedTextField(
            value = memo.title,
            onValueChange = {},
            label = { Text(stringResource(R.string.memo_title)) },
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = memo.description,
            onValueChange = {},
            label = { Text(stringResource(R.string.memo_text)) },
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        )
        Text(
            text = if (memo.hasLocation()) {
                stringResource(R.string.memo_location_format, memo.reminderLatitude, memo.reminderLongitude)
            } else {
                stringResource(R.string.memo_no_location)
            },
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}
