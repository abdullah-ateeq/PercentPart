package com.example.percentpart.util

import android.view.HapticFeedbackConstants
import android.view.View

object HapticHelper {
    fun performClickHaptic(view: View, enabled: Boolean) {
        if (!enabled) return
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    fun performActionHaptic(view: View, enabled: Boolean) {
        if (!enabled) return
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    }
}
