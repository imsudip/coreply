/**
 * coreply
 *
 * Copyright (C) 2024 coreply
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package app.coreply.coreplyapp.ui

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import app.coreply.coreplyapp.R
import app.coreply.coreplyapp.applistener.AppSupportStatus
import app.coreply.coreplyapp.compose.theme.CoreplyTheme
import app.coreply.coreplyapp.ui.compose.InlineSuggestionOverlay
import app.coreply.coreplyapp.ui.compose.LifeCycleThings
import app.coreply.coreplyapp.ui.compose.TrailingSuggestionOverlay
import app.coreply.coreplyapp.ui.viewmodel.OverlayViewModel
import app.coreply.coreplyapp.utils.PixelCalculator
import kotlin.math.min

/**
 * Created on 1/16/17.
 */

data class SuggestionParts(val inline: String?, val trailing: String?)

class Overlay(context: Context?) : ContextWrapper(context), ViewModelStoreOwner {

    private lateinit var pixelCalculator: PixelCalculator
    private lateinit var windowManager: WindowManager
    private lateinit var mainParams: WindowManager.LayoutParams
    private lateinit var trailingParams: WindowManager.LayoutParams
    private var chatEntry: Rect = Rect()
    private lateinit var inlineComposeView: ComposeView
    private lateinit var trailingComposeView: ComposeView
    private lateinit var viewModel: OverlayViewModel
    private var STATUSBAR_HEIGHT = 0
    private var DP8 = 0
    private var DP48 = 0
    private var DP20 = 0
    private var running = false

    private val dummyPaint: Paint = Paint()

    override val viewModelStore = ViewModelStore()
    private val lifeCycleThings = LifeCycleThings()

    init {
        this.setTheme(R.style.AppTheme)
        initialize()
    }


    private fun initialize() {
        pixelCalculator = PixelCalculator(this)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        mainParams = WindowManager.LayoutParams()
        trailingParams = WindowManager.LayoutParams()
        STATUSBAR_HEIGHT = resources.getDimensionPixelSize(
            resources.getIdentifier("status_bar_height", "dimen", "android")
        )
        DP8 = pixelCalculator.dpToPx(8)
        DP48 = pixelCalculator.dpToPx(48)
        DP20 = pixelCalculator.dpToPx(20)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[OverlayViewModel::class.java]

        // Create ComposeViews
        inlineComposeView = ComposeView(this).apply {

            setViewTreeLifecycleOwner(lifeCycleThings)
            setViewTreeSavedStateRegistryOwner(lifeCycleThings)
            setViewTreeViewModelStoreOwner(this@Overlay)
            setContent {

                CoreplyTheme {
                    val uiState = viewModel.uiState
                    if (uiState.inlineText.isNotBlank()) {
                        InlineSuggestionOverlay(
                            text = uiState.inlineText,
                            textSize = uiState.inlineTextSize,
                            showBackground = uiState.showBubbleBackground,
                            onClick = viewModel::onInlineClick,
                            onLongClick = viewModel::onInlineLongClick
                        )
                    }
                }
            }
        }

        trailingComposeView = ComposeView(this).apply {

            setViewTreeLifecycleOwner(lifeCycleThings)
            setViewTreeSavedStateRegistryOwner(lifeCycleThings)
            setViewTreeViewModelStoreOwner(this@Overlay)
            setContent {
                CoreplyTheme {
                    val uiState = viewModel.uiState
                    if (uiState.trailingText.isNotBlank()) {
                        TrailingSuggestionOverlay(
                            text = uiState.trailingText,
                            onClick = viewModel::onTrailingClick,
                            onLongClick = viewModel::onTrailingLongClick
                        )
                    }
                }
            }
        }

        // Configure window parameters for inline overlay
        mainParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        mainParams.flags =
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        mainParams.format = PixelFormat.TRANSLUCENT
        mainParams.gravity = Gravity.TOP or Gravity.START
        mainParams.height = DP48
        mainParams.alpha = 1.0f

        // Configure window parameters for trailing overlay
        trailingParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        trailingParams.flags =
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        trailingParams.format = PixelFormat.TRANSLUCENT
        trailingParams.gravity = Gravity.TOP or Gravity.START
        trailingParams.height = DP20
        trailingParams.alpha = 1.0f
        trailingParams.x = DP8
    }

    fun setRect(chatEntry: Rect) {
        this.chatEntry = chatEntry
    }

    fun setRect(top: Int, bottom: Int, left: Int, right: Int) {
        if (top != -1) chatEntry.top = top
        if (bottom != -1) chatEntry.bottom = bottom
        if (left != -1) chatEntry.left = left
        if (right != -1) chatEntry.right = right
    }

    fun setNode(node: AccessibilityNodeInfo, status: AppSupportStatus) {
        viewModel.setNode(node, status)
    }

    fun update() {
        if (running) {
            val uiState = viewModel.uiState

            // Update positioning
            mainParams.y = chatEntry.top - STATUSBAR_HEIGHT
            mainParams.height = chatEntry.bottom - chatEntry.top

//            // Update text size based on chat entry height
            val textSize = (chatEntry.bottom - chatEntry.top) * 0.8f
//            viewModel.updateTextSize(textSize)

            // Update chat entry width for text measurement
            viewModel.setChatEntryWidth(chatEntry.right - chatEntry.left)

            // Update background and positioning based on status
            val showBubbleBackground = uiState.showBubbleBackground
            viewModel.updateBackgroundVisibility(showBubbleBackground)
            val textWidth = dummyPaint.measureText(uiState.inlineText).toInt()

            if (showBubbleBackground) {
                // Calculate width based on text measurement (approximate)
                mainParams.width =
                    min(textWidth + DP8 * 3, chatEntry.right - chatEntry.left + DP8 * 2)
                mainParams.x = chatEntry.right - mainParams.width
            } else {
                // Calculate width based on text measurement (approximate)
                mainParams.width = min(textWidth+ DP8, chatEntry.right - chatEntry.left)
                mainParams.x = chatEntry.left
            }

            trailingParams.y = chatEntry.bottom - STATUSBAR_HEIGHT

            // Show/hide overlays based on content
            if (uiState.inlineText.isBlank()) {
                removeInlineOverlay()
            } else {
                showInlineOverlay()
            }

            if (uiState.trailingText.isBlank()) {
                removeTrailingOverlay()
            } else {
                // Calculate width based on text measurement (approximate)
                trailingParams.width = (textWidth * 1.1).toInt() + DP20
                showTrailingOverlay()
            }

            // Update view layouts if shown
            if (inlineComposeView.isShown) windowManager.updateViewLayout(
                inlineComposeView,
                mainParams
            )
            if (trailingComposeView.isShown) windowManager.updateViewLayout(
                trailingComposeView,
                trailingParams
            )
        }
    }

    fun updateSuggestion(suggestion: String?) {
        MainScope().launch {
            withContext(Dispatchers.Main) {
                viewModel.updateSuggestion(suggestion, dummyPaint.measureText(suggestion ?: ""))
                update()
                lifeCycleThings.refreshLifecycle()
            }
        }
    }

    fun updateTextSize(textSize: Float?) {
        if (textSize == null || textSize <= 0) return
        dummyPaint.textSize = textSize
        viewModel.updateTextSize(pixelCalculator.pxToSp(textSize))
        lifeCycleThings.refreshLifecycle()
    }

    fun disable() {
        running = false
        viewModel.disable()
        removeInlineOverlay()
        removeTrailingOverlay()
    }

    fun enable() {
        running = true
        viewModel.enable()
    }

    fun removeInlineOverlay() {
        if (inlineComposeView.isShown) {
            windowManager.removeView(inlineComposeView)
        }
    }

    fun removeTrailingOverlay() {
        if (trailingComposeView.isShown) {
            windowManager.removeView(trailingComposeView)
        }
    }

    fun showOverlays() {
        showInlineOverlay()
        showTrailingOverlay()
    }

    fun showInlineOverlay() {

        if (!inlineComposeView.isShown) {
            windowManager.addView(inlineComposeView, mainParams)
        }
    }

    fun showTrailingOverlay() {

        if (!trailingComposeView.isShown) {
            windowManager.addView(trailingComposeView, trailingParams)
        }
    }

}
