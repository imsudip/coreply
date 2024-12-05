package app.coreply.coreplyapp.utils

import android.content.Context
import android.content.ContextWrapper

/**
 * Created on 10/15/16.
 */

class PixelCalculator(context: Context?) : ContextWrapper(context) {
    fun dpToPx(dp: Int): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}