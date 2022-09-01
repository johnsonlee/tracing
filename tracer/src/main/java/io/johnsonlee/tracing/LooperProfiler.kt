package io.johnsonlee.tracing

import android.util.Printer
import androidx.tracing.Trace

class LooperProfiler(
    private val enabled: Boolean = true,
    private val profile: (Int, String, Long) -> Unit
) : Printer {

    private var t0: Long = 0L
    private var what: Int = 0

    override fun println(message: String) {
        if (!enabled || !Trace.isEnabled()) return

        if (message.startsWith(LOOP_ENTER_PREFIX)) {
            t0 = System.nanoTime()
            what = message.substringAfterLast(": ").toIntOrNull() ?: 0
        } else if (message.startsWith(LOOP_EXIT_PREFIX)) {
            val duration = System.nanoTime() - t0
            profile(what, buildLabel(message), duration)
        }
    }

}
