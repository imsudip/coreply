package app.coreply.coreplyapp.applistener

import android.view.accessibility.AccessibilityNodeInfo

fun generalTextInputFinder(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
    return node.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
}