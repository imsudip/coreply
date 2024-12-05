package app.coreply.coreplyapp.utils

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager

/**
 * Created on 1/13/17.
 */
object GlobalPref {
    fun isAccessibilityEnabled(context: Context?): Boolean {
        val manager =
            context!!.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val infos =
            manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        for (info in infos) {
            if (info.settingsActivityName != null && info.settingsActivityName == "app.coreply.coreplyapp.SettingsActivity") return true
        }
        return false
    }

    fun getFirstRunActivityPageNumber(context: Context?): Int {
        if (!Settings.canDrawOverlays(context)) return 1 //page=1 means enable draw over other apps page
        else if (!isAccessibilityEnabled(context)) {
            return 2 //page=2 means enable accessibility page
        }
        return 4
    }
}