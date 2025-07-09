package app.coreply.coreplyapp.applistener

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Created on 1/16/17.
 */
object SupportedApps {
    val supportedApps: Array<SupportedAppProperty> = arrayOf<SupportedAppProperty>(

        SupportedAppProperty(
            "com.whatsapp.w4b",
            makeGeneralDetector("com.whatsapp.w4b:id/entry"),
            { _, _, id, _ -> id == "com.whatsapp.w4b:id/entry" },
            null,
            arrayOf<String>("com.whatsapp.w4b/menuitem_delete"),
            { node: AccessibilityNodeInfo ->
                generalMessageListProcessor(
                    node,
                    arrayListOf("com.whatsapp.w4b:id/message_text", "com.whatsapp.w4b:id/caption")
                )
            },
            DetectedApp.WHATSAPP_BUSINESS
        ),
        SupportedAppProperty(
            "jp.naver.line.android",
            makeGeneralDetector("jp.naver.line.android:id/chat_ui_message_edit"),
            { _, _, id, _ -> id == "jp.naver.line.android:id/chat_ui_message_edit" },
            null,
            arrayOf<String>(),
            { node: AccessibilityNodeInfo ->
                generalMessageListProcessor(
                    node,
                    arrayListOf("jp.naver.line.android:id/chat_ui_message_text")
                )
            },
            DetectedApp.LINE
        ),

        SupportedAppProperty(
            "com.instagram.android",
            makeGeneralDetector("com.instagram.android:id/row_thread_composer_edittext"),
            { _, _, id, _ -> id == "com.instagram.android:id/row_thread_composer_edittext" },
            null,
            arrayOf<String>(),
            { node: AccessibilityNodeInfo ->
                generalMessageListProcessor(
                    node,
                    arrayListOf("com.instagram.android:id/direct_text_message_text_view")
                )
            },
            DetectedApp.INSTAGRAM
        ),

        SupportedAppProperty(
            "org.thoughtcrime.securesms",
            makeGeneralDetector("org.thoughtcrime.securesms:id/embedded_text_editor"),
            { _, _, id, _ -> id == "org.thoughtcrime.securesms:id/embedded_text_editor" },
            null,
            arrayOf<String>(),
            { node: AccessibilityNodeInfo ->
                generalMessageListProcessor(
                    node,
                    arrayListOf("org.thoughtcrime.securesms:id/conversation_item_body")
                )
            },
            DetectedApp.SIGNAL
        ),

        SupportedAppProperty(
            "co.hinge.app",
            makeGeneralDetector("co.hinge.app:id/messageComposition"),
            { _, _, id, _ -> id == "co.hinge.app:id/messageComposition" },
            null,
            arrayOf<String>(),
            { node: AccessibilityNodeInfo ->
                generalMessageListProcessor(
                    node,
                    arrayListOf("co.hinge.app:id/chatBubble")
                )
            },
            DetectedApp.HINGE
        ),
        SupportedAppProperty(
            "com.tinder",
            makeGeneralDetector("com.tinder:id/textMessageInput"),
            { _, _, id, _ -> id == "com.tinder:id/textMessageInput" },
            null,
            arrayOf<String>(),
            { node: AccessibilityNodeInfo ->
                generalMessageListProcessor(
                    node,
                    arrayListOf("com.tinder:id/chatTextMessageContent")
                )
            },
            DetectedApp.TINDER
        ),
        SupportedAppProperty(
            "com.vr.heymandi",
            makeGeneralDetector("com.vr.heymandi:id/messageInput"),
            { _, _, id, _ -> id == "com.vr.heymandi:id/messageInput" },
            null,
            arrayOf<String>(),
            { node: AccessibilityNodeInfo ->
                generalMessageListProcessor(
                    node,
                    arrayListOf("com.vr.heymandi:id/messageText")
                )
            },
            DetectedApp.HEYMANDI
        ),
        SupportedAppProperty(
            "com.google.android.gm",
            makeGeneralDetector("com.google.android.gm:id/inline_reply_compose_edit_text"),
            { _, _, id, _ -> id == "com.google.android.gm:id/inline_reply_compose_edit_text" },
            null,
            arrayOf<String>(),
            { node: AccessibilityNodeInfo ->
                generalMessageListProcessor(
                    node,
                    arrayListOf(
                        "com.google.android.gm:id/subject_and_folder_view",
                        "com.google.android.gm:id/email_snippet"
                    )
                )
            },
            DetectedApp.GMAIL
        ),
        SupportedAppProperty(
            "com.android.systemui",
            makeGeneralDetector(
                "com.android.systemui:id/expanded",//"com.android.systemui:id/expandableNotificationRow",
                returnTrigger = false
            ),
            { root, _, id, pkg -> root.findAccessibilityNodeInfosByViewId("com.android.systemui:id/expanded").isNotEmpty() },
            { node: AccessibilityNodeInfo -> generalTextInputFinder(node) },
            arrayOf<String>(),
            { node: AccessibilityNodeInfo ->
                notificationMessageListProcessor(node)
            },
            DetectedApp.ANDROID_SYSTEM
        ),
        SupportedAppProperty(
            "com.whatsapp",
            makeGeneralDetector("com.whatsapp:id/entry"),
            { _, _, id, _ -> id == "com.whatsapp:id/entry" },
            null,
            arrayOf<String>("com.whatsapp:id/menuitem_delete"),
            { node: AccessibilityNodeInfo ->
                generalMessageListProcessor(
                    node,
                    arrayListOf("com.whatsapp:id/message_text", "com.whatsapp:id/caption")
                )
            },
            DetectedApp.WHATSAPP
        ),
        SupportedAppProperty(
            "org.telegram.messenger",
            { node: AccessibilityNodeInfo, event: AccessibilityEvent? -> telegramDetector(node) },
            { root, focus, id, pkg -> pkg == "org.telegram.messenger" && telegramDetector(root).first },
            { node: AccessibilityNodeInfo -> generalTextInputFinder(node) },
            arrayOf<String>(),
            {
                telegramMessageListProcessor(it)
            },
            DetectedApp.TELEGRAM
        ),
        SupportedAppProperty(
            "com.mattermost.rn",
            makeGeneralDetector("channel.post_draft.post.input"),
            { _, _, id, _ -> id == "channel.post_draft.post.input" },
            null,
            arrayOf<String>(),
            { mattermostMessageListProcessor(it)
            },
            DetectedApp.MATTER_MOST

        )
        )
}
