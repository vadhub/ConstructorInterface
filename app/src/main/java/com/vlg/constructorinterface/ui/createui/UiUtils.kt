package com.vlg.constructorinterface.ui.createui

import android.content.Context

object UiUtils {
    @JvmStatic
    fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}