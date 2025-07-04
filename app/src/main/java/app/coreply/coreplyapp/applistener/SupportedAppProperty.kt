package app.coreply.coreplyapp.applistener

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Created on 1/18/17.
 */
data class SupportedAppProperty(
    val pkgName: String?, val triggerDetector: (AccessibilityNodeInfo, AccessibilityEvent?) -> Pair<Boolean, AccessibilityNodeInfo?>, val textInputFinder: ((AccessibilityNodeInfo) -> AccessibilityNodeInfo?)?, val excludeWidgets: Array<String>, val messageWidgets: Array<String>, val typeEnum: DetectedApp, val quotedMessageWidgets: String? = null
)
