package app.coreply.coreplyapp.applistener

/**
 * Created on 1/16/17.
 */
object SupportedApps {
    val supportedApps: Array<SupportedAppProperty?> = arrayOf<SupportedAppProperty?>(
        SupportedAppProperty(
            "com.whatsapp",
            "com.whatsapp:id/entry",
            arrayOf<String>("com.whatsapp:id/menuitem_delete"),
            arrayOf<String>("com.whatsapp:id/message_text", "com.whatsapp:id/caption"),
        ),
        SupportedAppProperty(
            "com.whatsapp.w4b",
            "com.whatsapp.w4b:id/entry",
            arrayOf<String>("com.whatsapp.w4b/menuitem_delete"),
            arrayOf<String>("com.whatsapp.w4b:id/message_text", "com.whatsapp.w4b:id/caption"),
        ),
// TODO: Support LINE
//        SupportedAppProperty(
//            "jp.naver.line.android",
//            "jp.naver.line.android:id/chathistory_message_edit",
//            arrayOf<String>(),
//            arrayOf<String>(),
//        ),

        SupportedAppProperty(
            "com.instagram.android",
            "com.instagram.android:id/row_thread_composer_edittext",
            arrayOf<String>(),
            arrayOf<String>("com.instagram.android:id/direct_text_message_text_view"),
        ),

        SupportedAppProperty(
            "co.hinge.app",
            "co.hinge.app:id/messageComposition",
            arrayOf<String>(),
            arrayOf<String>("co.hinge.app:id/chatBubble"),
        ),
        SupportedAppProperty(
            "com.tinder",
            "com.tinder:id/textMessageInput",
            arrayOf<String>(),
            arrayOf<String>("com.tinder:id/chatTextMessageContent"),
        ),

        )
}
