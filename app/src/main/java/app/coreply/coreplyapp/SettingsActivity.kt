package app.coreply.coreplyapp

import android.R
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat

/**
 * Created on 12/24/16.
 */
class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fragment: PreferenceFragmentCompat = SettingsFragment()
        supportFragmentManager.beginTransaction().replace(R.id.content, fragment).commit()
    }
}
