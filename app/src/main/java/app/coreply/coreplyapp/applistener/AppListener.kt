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
open class AppListener : AccessibilityService(), SuggestionUpdateListener {
    private var overlay: Overlay? = null
    private var pixelCalculator: PixelCalculator? = null
    private var currentApp: SupportedAppProperty? = null
    private var currentExcludeList = arrayOf<String>()
    private var running = false
    private var currentText: String? = null
    private val conversationList = ChatContents()
    open val ai by lazy { CallAI(SuggestionStorageClass(this), this) }

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
        overlay!!.disable()
    }

    private fun measureWindow(node: AccessibilityNodeInfo): AppSupportStatus {
        val rect = Rect()
        var status: AppSupportStatus = AppSupportStatus.SUPPORTED_UNKOWN
        node.getBoundsInScreen(rect)

        // Use EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY to get the cursor position
        val arguments = Bundle()
        arguments.putInt(
            AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_START_INDEX,
            0
        )
        arguments.putInt(
            AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_LENGTH,
            node.text?.length ?: 0
        )
        if (node.refreshWithExtraData(
                AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY,
                arguments
            )
        ) {
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                val rectArray: Array<RectF?>? = node.extras.getParcelableArray(
                    AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY,
                    RectF::class.java
                )
                // For loop in reverse order to get the last cursor position
                if (rectArray != null && rectArray.any { it != null }) {
                    status = AppSupportStatus.TYPING
                    for (i in rectArray.indices.reversed()) {
                        val rectF = rectArray[i]
                        if (rectF != null) {
                            rect.left = rectF.right.toInt()
                            rect.top = rectF.top.toInt()
                            rect.bottom = rectF.bottom.toInt()
                            break
                        } // Otherise, is a fake text, probably hint text
                    }
                } else {
                    rect.left += (rect.width()*0.5).toInt()
                    status = AppSupportStatus.HINT_TEXT
                }
            } else {
                status = AppSupportStatus.API_BELOW_33
            }

        } else {
            Log.v("CoWA", "Failed to refresh cursor position")
        }

        if (node.refreshWithExtraData(AccessibilityNodeInfo.EXTRA_DATA_RENDERING_INFO_KEY, arguments)){
            overlay!!.updateTextSize(node.extraRenderingInfo?.textSizeInPx)
        }
        overlay!!.setRect(rect)
        overlay!!.update()
        return status
    }

    private fun onEditTextUpdate(node: AccessibilityNodeInfo, status: AppSupportStatus) {
        var actualMessage = node.text?.toString()?.replace("Compose Message", "") ?: ""
        if (status == AppSupportStatus.HINT_TEXT || node.isShowingHintText) {
            actualMessage = ""
        }
        currentText = actualMessage
        if (ai.suggestionStorage.getSuggestion(actualMessage) != null) {
            overlay!!.updateSuggestion(ai.suggestionStorage.getSuggestion(actualMessage))
        } else {
            overlay!!.updateSuggestion("")
            ai.onUserInputChanged(TypingInfo(conversationList, actualMessage))
        }
        overlay!!.setNode(node,status)
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
                overlay!!.enable()

                val triggerWidget = triggerWidgetList.get(0)

                val status = measureWindow(triggerWidget)
                var actualMessage =
                    triggerWidget.getText()?.toString()?.replace("Compose Message", "") ?: ""
                if (actualMessage == "Message") {
                    actualMessage = ""
                }
                if (actualMessage != currentText) {
                    if (getMessages(root)) {
                        ai.suggestionStorage.clearSuggestion()
                    }
                    onEditTextUpdate(triggerWidget, status)
                } else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && event.getPackageName() == currentApp!!.pkgName) {
                    val refreshMessageList = getMessages(root)
                    if (refreshMessageList || actualMessage != currentText) {
                        onEditTextUpdate(triggerWidget, status)
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
                overlay!!.disable()
                autoDetect = false
                running = false
                ai.suggestionStorage.clearSuggestion()
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

        val rootRect = Rect()
        rootInActiveWindow.getBoundsInScreen(rootRect)
        for (chatNodeInfo in chatWidgets) {
            val bounds = Rect()
            chatNodeInfo.getBoundsInScreen(bounds)
            val isMe = (bounds.left+bounds.right)/2 > (rootRect.left+rootRect.right)/2
            val message_text = chatNodeInfo.text?.toString() ?: ""
            chatMessages.add(ChatMessage(if (isMe) "Me" else "Others", message_text, ""))
        }

        //Log.v("CoWA", conversationList.toString())
        return conversationList.combineChatContents(chatMessages)
    }


    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = this.getServiceInfo()

        info.eventTypes =
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or AccessibilityEvent.TYPE_VIEW_CLICKED or AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or AccessibilityEvent.TYPE_VIEW_FOCUSED
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        this.setServiceInfo(info)
        Toast.makeText(this, getString(R.string.app_accessibility_started), Toast.LENGTH_SHORT)
            .show()
        val appContext = applicationContext

        if (overlay == null) {
            overlay = Overlay(appContext)
            overlay!!.disable()
        } else {
            overlay!!.disable()
        }
        pixelCalculator = PixelCalculator(appContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (overlay != null) overlay!!.disable()
    }

    override fun onSuggestionUpdated(typingInfo: TypingInfo, newSuggestion: String) {
        if (overlay != null && running) {
            overlay!!.updateSuggestion(ai.suggestionStorage.getSuggestion(currentText!!))
        }
    }

    companion object {
        var autoDetect: Boolean = false
    }
}
