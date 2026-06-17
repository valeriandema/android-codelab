package com.sap.codelab.utils.extensions

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding

/**
 * Pads this view with the status-bar inset so it is no longer drawn behind the (edge-to-edge)
 * status bar enforced from Android 15. Intended for the top-most app bar.
 */
internal fun View.applyStatusBarTopPadding() {
    val initialTop = paddingTop
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val top = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top
        view.updatePadding(top = initialTop + top)
        windowInsets
    }
}

/**
 * Adds the navigation-bar (and keyboard) inset as bottom padding so scrollable content stays clear
 * of the edge-to-edge navigation bar. Applied on top of the view's original padding.
 */
internal fun View.applyNavigationBarBottomPadding() {
    val initialBottom = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val bottom = windowInsets.getInsets(
            WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.ime()
        ).bottom
        view.updatePadding(bottom = initialBottom + bottom)
        windowInsets
    }
}

/**
 * Adds the navigation-bar (and keyboard) inset as bottom margin so a floating view such as the FAB
 * stays clear of the edge-to-edge navigation bar. Applied on top of the view's original margin.
 */
internal fun View.applyNavigationBarBottomMargin() {
    val initialBottom = (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val bottom = windowInsets.getInsets(
            WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.ime()
        ).bottom
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> { bottomMargin = initialBottom + bottom }
        windowInsets
    }
}
