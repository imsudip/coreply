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

package app.coreply.coreplyapp.applistener

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import app.coreply.coreplyapp.R
import app.coreply.coreplyapp.network.CallAI
import app.coreply.coreplyapp.network.SuggestionStorageClass
import app.coreply.coreplyapp.network.TypingInfo
import app.coreply.coreplyapp.ui.Overlay
import app.coreply.coreplyapp.utils.ChatContents
import app.coreply.coreplyapp.utils.ChatMessage
import app.coreply.coreplyapp.utils.PixelCalculator
import app.coreply.coreplyapp.utils.SuggestionUpdateListener
import java.util.ArrayList
import java.util.Comparator

/**
 * Created on 10/13/16.
 */
class AppListener : AccessibilityService(), SuggestionUpdateListener {
    private var overlay: Overlay? = null
    private var pixelCalculator: PixelCalculator? = null
    private var currentApp: SupportedAppProperty? = null
    private var currentExcludeList = arrayOf<String>()
    private var running = false
    private var currentText: String? = null
    private val conversationList = ChatContents()
    private val ai = CallAI(SuggestionStorageClass(this))

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.v("CoWA", "event triggered")
        if (event != null && event.getPackageName() != null) {
            Log.v("CoWA", event.getPackageName().toString())
        }
        if (event != null && event.getClassName() != null) {
            Log.v("CoWA", event.getClassName().toString())
        }
        if (event == null || event.getPackageName() == null || event.getClassName() == null) {
            Log.v("CoWA", "Either event or package name or class name is null")
            return
        }
        val root1 = rootInActiveWindow
        if (root1 == null) {
            Log.v("CoWA", "root is null")
        } else {
            refreshOverlay(event, root1)
        }
    }


    override fun onInterrupt() {
        overlay!!.removeViews()
    }

    private fun measureWindow(node: AccessibilityNodeInfo) {
        val rect = Rect()
        node.getBoundsInScreen(rect)
        // Use EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY to get the cursor position
        val arguments = Bundle()
        arguments.putInt(
            AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_START_INDEX,
            0
        )
        arguments.putInt(
            AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_LENGTH,
            node.getText().length
        )
        if (node.refreshWithExtraData(
                AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY,
                arguments
            )
        ) {
            val rectArray: Array<RectF?>? = node.extras.getParcelableArray<RectF?>(
                AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY,
                RectF::class.java
            )
            // For loop in reverse order to get the last cursor position
            if (rectArray != null) {
                for (i in rectArray.indices.reversed()) {
                    val rectF = rectArray[i]
                    if (rectF != null) {
                        rect.left = rectF.right.toInt()
                        rect.top = rectF.top.toInt()
                        rect.bottom = rectF.bottom.toInt()

                        break
                    } else {
                        Log.v("CoWA", "Rect is null")
                    }
                }
            } else {
                Log.v("CoWA", "Rect array is null")
            }
        } else {
            Log.v("CoWA", "Failed to refresh cursor position")
        }
        if (node.isShowingHintText() || node.getText().toString() == "Message") {
            rect.left = rect.left + 200
        }
        Log.v("CoWA", "Text Node: " + node.getText())
        overlay!!.setRect(rect)
        overlay!!.update()
    }

    private fun onEditTextUpdate(node: AccessibilityNodeInfo) {
        var actualMessage = node.getText().toString().replace("Compose Message", "")
        if (actualMessage == "Message" || node.isShowingHintText()) {
            actualMessage = ""
        }
        currentText = actualMessage
        if (ai.suggestionStorage.getSuggestion(actualMessage) != null) {
            overlay!!.updateSuggestion(ai.suggestionStorage.getSuggestion(actualMessage))
        } else {
            overlay!!.updateSuggestion("")
            ai.onUserInputChanged(TypingInfo(conversationList, actualMessage))
        }
        overlay!!.setNode(node)
    }

    private fun refreshOverlay(event: AccessibilityEvent, root: AccessibilityNodeInfo): Boolean {
        var isSupportedApp = false
        for (supportedAppProperty in SupportedApps.supportedApps) {
            val triggerWidgetList =
                root.findAccessibilityNodeInfosByViewId(supportedAppProperty!!.triggerWidget!!)
            if (triggerWidgetList != null && triggerWidgetList.size == 1) { // Only one trigger widget is supported
                isSupportedApp = true
                currentApp = supportedAppProperty
                currentExcludeList = supportedAppProperty.excludeWidgets
                running = true

                val triggerWidget = triggerWidgetList.get(0)

                measureWindow(triggerWidget)
                var actualMessage =
                    triggerWidget.getText().toString().replace("Compose Message", "")
                if (actualMessage == "Message") {
                    actualMessage = ""
                }
                if (actualMessage != currentText) {
                    if (getMessages(root)) {
                        ai.suggestionStorage.clearSuggestion()
                    }
                    onEditTextUpdate(triggerWidget)
                } else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && event.getPackageName() == currentApp!!.pkgName) {
                    val refreshMessageList = getMessages(root)
                    if (refreshMessageList || actualMessage != currentText) {
                        onEditTextUpdate(triggerWidget)
                    }
                    if (refreshMessageList) {
                        ai.suggestionStorage.clearSuggestion()
                    }
                }
                break
            }
        }
        if (!isSupportedApp) {
            if (running) {
                autoDetect = false
                overlay!!.removeViews()
                running = false
                currentText = null
            }
        }
        return isSupportedApp
    }

    private fun getMessages(rootInActiveWindow: AccessibilityNodeInfo): Boolean {
        val chatWidgets: MutableList<AccessibilityNodeInfo> = ArrayList<AccessibilityNodeInfo>()
        val chatMessages: MutableList<ChatMessage> = ArrayList<ChatMessage>()

        for (messageWidget in currentApp!!.messageWidgets) {
            chatWidgets.addAll(rootInActiveWindow.findAccessibilityNodeInfosByViewId(messageWidget))
        }
        chatWidgets.sortWith(Comparator { o1: AccessibilityNodeInfo?, o2: AccessibilityNodeInfo? ->
            val rect1 = Rect()
            val rect2 = Rect()
            o1!!.getBoundsInScreen(rect1)
            o2!!.getBoundsInScreen(rect2)
            rect1.top - rect2.top
        })

        var left = -1
        var right = -1
        for (chatNodeInfo in chatWidgets) {
            val bounds = Rect()
            chatNodeInfo.getBoundsInScreen(bounds)
            if (left == -1) {
                left = bounds.left
                right = bounds.right
            } else {
                if (bounds.left < left) {
                    left = bounds.left
                }
                if (bounds.right > right) {
                    right = bounds.right
                }
            }
        }
        for (chatNodeInfo in chatWidgets) {
            val bounds = Rect()
            chatNodeInfo.getBoundsInScreen(bounds)
            val isMe = bounds.right == right
            val message_text = chatNodeInfo.getText().toString()
            chatMessages.add(ChatMessage(if (isMe) "Me" else "Others", message_text, ""))
        }

        Log.v("CoWA", conversationList.toString())
        return conversationList.combineChatContents(chatMessages)
    }


    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = this.getServiceInfo()

        info.eventTypes =
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or AccessibilityEvent.TYPE_VIEW_CLICKED or AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or AccessibilityEvent.TYPE_VIEW_FOCUSED
        this.setServiceInfo(info)
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        Toast.makeText(this, getString(R.string.app_accessibility_started), Toast.LENGTH_SHORT)
            .show()
        val appContext = applicationContext

        if (overlay == null) {
            overlay = Overlay(appContext)
            overlay!!.removeViews()
        } else {
            overlay!!.removeViews()
        }
        pixelCalculator = PixelCalculator(appContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (overlay != null) overlay!!.removeViews()
    }

    override fun onSuggestionUpdated(typingInfo: TypingInfo, newSuggestion: String) {
        Log.v("CoWA", "Suggestion updated")
        Log.v("CoWA", typingInfo.currentTyping)
        Log.v("CoWA", currentText!!)
        Log.v("CoWA", newSuggestion)
        if (overlay != null) {
            overlay!!.updateSuggestion(ai.suggestionStorage.getSuggestion(currentText!!))
        }
    }

    companion object {
        var autoDetect: Boolean = false
    }
}
