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

package app.coreply.coreplyapp.ui.viewmodel

import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import app.coreply.coreplyapp.applistener.AppSupportStatus
import android.icu.text.BreakIterator
import java.util.Locale

data class OverlayUiState(
    val inlineText: String = "",
    val trailingText: String = "",
    val inlineTextSize: Float = 18f,
    val showBubbleBackground: Boolean = false,
    val isRunning: Boolean = false
)

class OverlayViewModel : ViewModel() {
    
    var uiState by mutableStateOf(OverlayUiState())
        private set
    
    private var node: AccessibilityNodeInfo? = null
    private var status: AppSupportStatus = AppSupportStatus.UNSUPPORTED
    private var chatEntryWidth: Int = 0


    fun setNode(node: AccessibilityNodeInfo, status: AppSupportStatus) {
        this.node = node
        this.status = status
    }

    fun setChatEntryWidth(width: Int) {
        this.chatEntryWidth = width
    }

    fun updateTextSize(textSize: Float) {
        uiState = uiState.copy(inlineTextSize = textSize)
    }

    fun updateBackgroundVisibility(showBackground: Boolean) {
        uiState = uiState.copy(showBubbleBackground = showBackground)
    }

    fun updateSuggestion(suggestion: String?, textActualWidth: Float) {
        val suggestion = suggestion ?: ""
        
        when {
            status == AppSupportStatus.API_BELOW_33 -> {
                uiState = uiState.copy(
                    inlineText = "",
                    trailingText = suggestion.trimEnd(),
                    showBubbleBackground = false
                )
            }
            textActualWidth > chatEntryWidth -> {
                uiState = uiState.copy(
                    inlineText = suggestion.trimEnd(),
                    trailingText = suggestion.trimEnd(),
                    showBubbleBackground = status == AppSupportStatus.HINT_TEXT
                )
            }
            else -> {
                uiState = uiState.copy(
                    inlineText = suggestion.trimEnd(),
                    trailingText = "",
                    showBubbleBackground = status == AppSupportStatus.HINT_TEXT
                )
            }
        }
    }

    fun enable() {
        uiState = uiState.copy(isRunning = true)
    }

    fun disable() {
        uiState = uiState.copy(
            isRunning = false,
            inlineText = "",
            trailingText = ""
        )
    }

    fun onInlineClick() {
        performTextAction(uiState.inlineText)
    }

    fun onInlineLongClick() {
        performFullTextAction(uiState.inlineText)
    }

    fun onTrailingClick() {
        performTextAction(uiState.trailingText)
    }

    fun onTrailingLongClick() {
        performFullTextAction(uiState.trailingText)
    }

    private fun tokenizeText(input: String): List<String> {
        val breakIterator = BreakIterator.getWordInstance(Locale.ROOT)
        breakIterator.setText(input)
        val tokens = mutableListOf<String>()
        var start = breakIterator.first()
        var end = breakIterator.next()
        while (end != BreakIterator.DONE) {
            val word = input.substring(start, end)
            if (word.isNotEmpty()) {
                tokens.add(word)
            }
            start = end
            end = breakIterator.next()
        }
        return tokens
    }

    private fun performTextAction(text: String) {
        val arguments = Bundle()
        val tokenizedString = tokenizeText(text.trimEnd())
        var addText: String = if (tokenizedString.isNotEmpty()) tokenizedString[0] else ""
        if (addText.isBlank() && tokenizedString.size > 1) {
            addText += tokenizedString[1]
        }
        
        if (node?.isShowingHintText == true || status == AppSupportStatus.HINT_TEXT) {
            arguments.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, 
                addText
            )
        } else {
            arguments.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                node?.text?.toString()?.replace("Compose Message", "") + addText
            )
        }
        node?.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }

    private fun performFullTextAction(text: String) {
        val arguments = Bundle()
        if (node?.isShowingHintText == true || status == AppSupportStatus.HINT_TEXT) {
            arguments.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text.trimEnd()
            )
        } else {
            arguments.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                node?.text?.toString()?.replace("Compose Message", "") + text.trimEnd()
            )
        }
        node?.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }
}
