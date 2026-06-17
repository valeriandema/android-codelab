package com.sap.codelab

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sap.codelab.ui.theme.CodelabTheme
import com.sap.codelab.view.CodelabNavHost
import dagger.hilt.android.AndroidEntryPoint

const val EXTRA_MEMO_ID: String = "memoId"

@AndroidEntryPoint
internal class MainActivity : ComponentActivity() {

    private var pendingMemoId by mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingMemoId = intent.memoIdExtra()
        setContent {
            CodelabTheme {
                CodelabNavHost(
                    deepLinkMemoId = pendingMemoId,
                    onDeepLinkHandled = { pendingMemoId = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingMemoId = intent.memoIdExtra()
    }
}

private fun Intent.memoIdExtra(): Long? =
    if (hasExtra(EXTRA_MEMO_ID)) getLongExtra(EXTRA_MEMO_ID, -1L).takeIf { it >= 0 } else null
