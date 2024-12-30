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
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import app.coreply.coreplyapp.R
import app.coreply.coreplyapp.utils.PixelCalculator

/**
 * Created on 1/16/17.
 */

data class SuggestionParts(val inline: String?, val trailing: String?)

class Overlay(context: Context?) : ContextWrapper(context), View.OnClickListener,
    View.OnLongClickListener {

    private lateinit var pixelCalculator: PixelCalculator
    private lateinit var windowManager: WindowManager
    private lateinit var mainParams: WindowManager.LayoutParams
    private lateinit var trailingParams: WindowManager.LayoutParams
    private var chatEntry: Rect = Rect()
    private lateinit var inlineView: View
    private lateinit var trailingView: View
    private lateinit var inlineTextView: TextView
    private lateinit var trailingTextView: TextView
    private var STATUSBAR_HEIGHT = 0
    private var DP8 = 0
    private var DP48 = 0
    private var DP20 = 0
    private var node: AccessibilityNodeInfo? = null
    private var isHintText: Boolean = false

    init {
        this.setTheme(R.style.AppTheme)
        initialize()
    }

    private fun initialize() {
        pixelCalculator = PixelCalculator(this)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        mainParams = WindowManager.LayoutParams()
        trailingParams = WindowManager.LayoutParams()
        STATUSBAR_HEIGHT = getResources().getDimensionPixelSize(
            getResources().getIdentifier("status_bar_height", "dimen", "android")
        )
        DP8 = pixelCalculator.dpToPx(8)
        DP48 = pixelCalculator.dpToPx(48)
        DP20 = pixelCalculator.dpToPx(20)

        val wrapper = ContextThemeWrapper(this, R.style.AppTheme)
        val layoutInflater = LayoutInflater.from(wrapper)
        inlineView = layoutInflater.inflate(R.layout.overlay_main, null)
        trailingView = layoutInflater.inflate(R.layout.overlay_trailing, null)

        mainParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        mainParams.flags =
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        mainParams.format = PixelFormat.TRANSLUCENT
        mainParams.gravity = Gravity.TOP or Gravity.START
        mainParams.height = DP48
        mainParams.alpha = 0.8f

        trailingParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        trailingParams.flags =
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        trailingParams.format = PixelFormat.TRANSLUCENT
        trailingParams.gravity = Gravity.TOP or Gravity.START
        trailingParams.height = DP20
        trailingParams.alpha = 0.65f
        trailingParams.x = DP8

        inlineTextView = inlineView.findViewById<TextView>(R.id.suggestionBtn)
        trailingTextView = trailingView.findViewById<TextView>(R.id.trailingSuggestions)

        inlineTextView.setOnClickListener(this)
        trailingTextView.setOnClickListener(this)
        inlineTextView.setOnLongClickListener(this)
        trailingTextView.setOnLongClickListener(this)

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

    fun setNode(node: AccessibilityNodeInfo, isHintText: Boolean) {
        this.node = node
        this.isHintText = isHintText
    }

    fun update() {
        mainParams.x = chatEntry.left
        mainParams.y = chatEntry.top - STATUSBAR_HEIGHT
        mainParams.height =
            chatEntry.bottom - chatEntry.top
        mainParams.width = chatEntry.right - chatEntry.left

        trailingParams.y = chatEntry.bottom - STATUSBAR_HEIGHT

        if (inlineTextView.text.isBlank()) {
            removeInlineOverlay()
        } else {
            showInlineOverlay()
        }

        if (trailingTextView.text.isBlank()) {
            removeTrailingOverlay()
        } else {
            trailingParams.width =
                (trailingTextView.paint.measureText(trailingTextView.text.toString()) * 1.1).toInt() + DP20
            showTrailingOverlay()
        }


        if (inlineView.isShown()) windowManager.updateViewLayout(inlineView, mainParams)
        if (trailingView.isShown()) windowManager.updateViewLayout(trailingView, trailingParams)
    }

    fun updateSuggestion(suggestion: String?) {
        var suggestion = suggestion ?: ""

        MainScope().launch {
            withContext(Dispatchers.Main) {
                if (inlineTextView.paint.measureText(suggestion) > mainParams.width) {
                    inlineTextView.text = suggestion.toString().trimEnd()
                    trailingTextView.text = suggestion.toString().trimEnd()
                } else {
                    inlineTextView.text = suggestion.toString().trimEnd()
                    trailingTextView.text = ""
                }
                update()
            }
        }

    }

    fun removeViews() {
        removeInlineOverlay()
        removeTrailingOverlay()
    }

    fun removeInlineOverlay() {
        if (inlineView.isShown) {
            windowManager.removeView(inlineView)
        }
    }

    fun removeTrailingOverlay() {
        if (trailingView.isShown) {
            windowManager.removeView(trailingView)
        }
    }


    fun showOverlays() {
        showInlineOverlay()
        showTrailingOverlay()
    }

    fun showInlineOverlay() {
        if (!inlineView.isShown) {
            windowManager.addView(inlineView, mainParams)
        }
    }

    fun showTrailingOverlay() {
        if (!trailingView.isShown) {
            windowManager.addView(trailingView, trailingParams)
        }
    }


    override fun onClick(v: View) {
        Log.v("CoWA", "onClick")
        Log.v("CoWA", (v as TextView).getText().toString().trimEnd().split(" ")[0])

        if (v.getId() == R.id.suggestionBtn || v.id == R.id.trailingSuggestions) {
            val arguments = Bundle()
            var addText: String = (v as TextView).getText().toString().trimEnd().split(" ")[0]
            if (addText.isBlank()) {
                addText = " " + (v as TextView).getText().toString().trimEnd().split(" ")[1]
            }
            if (node!!.isShowingHintText() || isHintText) {
                arguments.putCharSequence(
                    AccessibilityNodeInfo
                        .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, addText

                )
            } else {
                arguments.putCharSequence(
                    AccessibilityNodeInfo
                        .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    node!!.text.toString()
                        .replace("Compose Message", "") + addText
                )
            }
            node!!.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        }
    }

    override fun onLongClick(v: View): Boolean {
        if (v.getId() == R.id.suggestionBtn || v.id == R.id.trailingSuggestions) {
            val arguments = Bundle()
            if (node!!.isShowingHintText() || isHintText) {
                arguments.putCharSequence(
                    AccessibilityNodeInfo
                        .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    (v as TextView).getText().toString().trimEnd()
                )
            } else {
                arguments.putCharSequence(
                    AccessibilityNodeInfo
                        .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    node!!.getText().toString()
                        .replace("Compose Message", "") + (v as TextView).getText().toString()
                        .trimEnd()
                )
            }
            node!!.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        }
        return true
    }
}
