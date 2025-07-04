package app.coreply.coreplyapp.applistener

import android.view.accessibility.AccessibilityNodeInfo

/**
 * Created on 1/16/17.
 */
object SupportedApps {
    val supportedApps: Array<SupportedAppProperty> = arrayOf<SupportedAppProperty>(
        SupportedAppProperty(
            "com.whatsapp",
            makeGeneralDetector("com.whatsapp:id/entry"),
            null,
            arrayOf<String>("com.whatsapp:id/menuitem_delete"),
            arrayOf<String>("com.whatsapp:id/message_text", "com.whatsapp:id/caption"),
            DetectedApp.WHATSAPP
        ),
        SupportedAppProperty(
            "com.whatsapp.w4b",
            makeGeneralDetector("com.whatsapp.w4b:id/entry"),
            null,
            arrayOf<String>("com.whatsapp.w4b/menuitem_delete"),
            arrayOf<String>("com.whatsapp.w4b:id/message_text", "com.whatsapp.w4b:id/caption"),
            DetectedApp.WHATSAPP_BUSINESS
        ),
        SupportedAppProperty(
            "jp.naver.line.android",
            makeGeneralDetector("jp.naver.line.android:id/chat_ui_message_edit"),
            null,
            arrayOf<String>(),
            arrayOf<String>("jp.naver.line.android:id/chat_ui_message_text"),
            DetectedApp.LINE
        ),

        SupportedAppProperty(
            "com.instagram.android",
            makeGeneralDetector("com.instagram.android:id/row_thread_composer_edittext"),
            null,
            arrayOf<String>(),
            arrayOf<String>("com.instagram.android:id/direct_text_message_text_view"),
            DetectedApp.INSTAGRAM
        ),

        SupportedAppProperty(
            "org.thoughtcrime.securesms",
            makeGeneralDetector("org.thoughtcrime.securesms:id/embedded_text_editor"),
            null,
            arrayOf<String>(),
            arrayOf<String>("org.thoughtcrime.securesms:id/conversation_item_body"),
            DetectedApp.SIGNAL
        ),

        SupportedAppProperty(
            "co.hinge.app",
            makeGeneralDetector("co.hinge.app:id/messageComposition"),
            null,
            arrayOf<String>(),
            arrayOf<String>("co.hinge.app:id/chatBubble"),
            DetectedApp.HINGE
        ),
        SupportedAppProperty(
            "com.tinder",
            makeGeneralDetector("com.tinder:id/textMessageInput"),
            null,
            arrayOf<String>(),
            arrayOf<String>("com.tinder:id/chatTextMessageContent"),
            DetectedApp.TINDER
        ),
        SupportedAppProperty(
            "com.vr.heymandi",
            makeGeneralDetector("com.vr.heymandi:id/messageInput"),
            null,
            arrayOf<String>(),
            arrayOf<String>("com.vr.heymandi:id/messageText"),
            DetectedApp.HEYMANDI
        ),
        SupportedAppProperty(
            "com.google.android.gm",
            makeGeneralDetector("com.google.android.gm:id/inline_reply_compose_edit_text"),
            null,
            arrayOf<String>(),
            arrayOf<String>("com.google.android.gm:id/subject_and_folder_view","com.google.android.gm:id/email_snippet"),
            DetectedApp.GMAIL
        ),
        SupportedAppProperty(
            "com.android.systemui",
            makeGeneralDetector("com.android.systemui:id/expandableNotificationRow"),
            {node: AccessibilityNodeInfo -> generalTextInputFinder(node) },
            arrayOf<String>(),
            arrayOf<String>("android:id/message_text"),
            DetectedApp.ANDROID_SYSTEM
        ),

        )
}
