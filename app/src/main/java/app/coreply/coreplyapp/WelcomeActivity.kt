package app.coreply.coreplyapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import app.coreply.coreplyapp.utils.GlobalPref.isAccessibilityEnabled

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val page = intent.getIntExtra("page", 0)
        when (page) {
            1 -> grantOverlay()
            2 -> grantAccessibility()
            3 -> disableAccessibility()
            else -> {
                Toast.makeText(this, R.string.tutorial_permission_done, Toast.LENGTH_LONG)
                    .show() //TODO: strings.xml
                finish()
            }
        }
    }

    fun grantOverlay() {
        setContentView(R.layout.activity_overlay_permission)
    }

    fun openOverlaySettings(v: View?) {
        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
        if (!isAccessibilityEnabled(this)) grantAccessibility()
        else finish()

    }

    fun grantAccessibility() {
        setContentView(R.layout.activity_accessibility_permission)
    }

    fun disableAccessibility() {
        setContentView(R.layout.activity_accessibility_disable_permission)
    }

    // TODO: Implement openVideoTutorial
    fun openVideoTutorial(v: View?) {
        val id = ""
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$id"))
        try {
            startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/$id"))
            startActivity(webIntent)
        }
    }

    fun openAccessibilitySettings(v: View?) {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        finish()
    }
}
