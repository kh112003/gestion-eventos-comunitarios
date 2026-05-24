package com.grupo.gestioneventos

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

fun View.applySystemBarPadding(
    applyTop: Boolean = false,
    applyBottom: Boolean = false,
    applyLeft: Boolean = false,
    applyRight: Boolean = false
) {
    val initialLeft = paddingLeft
    val initialTop = paddingTop
    val initialRight = paddingRight
    val initialBottom = paddingBottom

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(
            left = initialLeft + if (applyLeft) bars.left else 0,
            top = initialTop + if (applyTop) bars.top else 0,
            right = initialRight + if (applyRight) bars.right else 0,
            bottom = initialBottom + if (applyBottom) bars.bottom else 0
        )
        insets
    }
    ViewCompat.requestApplyInsets(this)
}

fun View.applyNavigationBarBottomMargin(extraBottomMargin: Int = 0) {
    val initialBottomMargin = (layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin ?: 0

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        val params = view.layoutParams as? ViewGroup.MarginLayoutParams
        if (params != null) {
            params.bottomMargin = initialBottomMargin + extraBottomMargin + bars.bottom
            view.layoutParams = params
        }
        insets
    }
    ViewCompat.requestApplyInsets(this)
}

fun View.applySystemBarsAndImePadding(
    applyTop: Boolean = false,
    applyBottom: Boolean = false,
    applyLeft: Boolean = false,
    applyRight: Boolean = false
) {
    val initialLeft = paddingLeft
    val initialTop = paddingTop
    val initialRight = paddingRight
    val initialBottom = paddingBottom

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
        view.updatePadding(
            left = initialLeft + if (applyLeft) bars.left else 0,
            top = initialTop + if (applyTop) bars.top else 0,
            right = initialRight + if (applyRight) bars.right else 0,
            bottom = initialBottom + if (applyBottom) maxOf(bars.bottom, ime.bottom) else 0
        )
        insets
    }
    ViewCompat.requestApplyInsets(this)
}

fun ScrollView.keepFocusedViewVisible(target: View, extraBottomSpace: Int = 48) {
    target.setOnFocusChangeListener { _, hasFocus ->
        if (hasFocus) {
            scrollToChild(target, extraBottomSpace)
            postDelayed({ scrollToChild(target, extraBottomSpace) }, 300)
        }
    }
}

private fun ScrollView.scrollToChild(target: View, extraBottomSpace: Int) {
    val rect = Rect()
    target.getDrawingRect(rect)
    offsetDescendantRectToMyCoords(target, rect)

    val visibleBottom = scrollY + height - paddingBottom - extraBottomSpace
    if (rect.bottom > visibleBottom) {
        smoothScrollTo(0, scrollY + rect.bottom - visibleBottom)
    }
}
