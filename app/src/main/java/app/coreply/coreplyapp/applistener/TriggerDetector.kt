package app.coreply.coreplyapp.applistener

import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

enum class DetectedApp {
    WHATSAPP,
    WHATSAPP_BUSINESS,
    LINE,
    INSTAGRAM,
    SIGNAL,
    HINGE,
    TINDER,
    HEYMANDI,
    GMAIL,
    ANDROID_SYSTEM,
    NOT_DETECTED
}

fun makeGeneralDetector(targetId: String, returnTrigger: Boolean = true): (AccessibilityNodeInfo, AccessibilityEvent?) -> Pair<Boolean, AccessibilityNodeInfo?> {
    return { node: AccessibilityNodeInfo, event: AccessibilityEvent? ->
        generalDetector(node, targetId, returnTrigger)
    }
}

fun generalDetector(node: AccessibilityNodeInfo, targetId: String, returnTrigger: Boolean = true): Pair<Boolean, AccessibilityNodeInfo?> {
    val triggerWidgetList =
        node.findAccessibilityNodeInfosByViewId(targetId)
    //iterNode(node)
    Log.v("CoWA", "generalDetector: targetId=$targetId, triggerWidgetList.size=${triggerWidgetList.size}")
    if (triggerWidgetList != null && triggerWidgetList.isNotEmpty()) { // Only one trigger widget is supported
        return Pair(true, if (returnTrigger) triggerWidgetList[0] else null)
    } else {
        return Pair(false, null)
    }

}

fun iterNode(node: AccessibilityNodeInfo){
    Log.v("CoWA", "iterNode: node=${node.className}, text=${node.text}, contentDescription=${node.contentDescription}, viewId=${node.viewIdResourceName}")
    for (i in 0 until node.childCount) {
        val child = node.getChild(i)
        if (child != null) {
            iterNode(child)
        }
    }
}