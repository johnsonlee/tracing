package io.johnsonlee.tracing

import android.util.Printer
import androidx.tracing.Trace

private const val MAX_LABEL_LENGTH = 127

class LooperTracer(
    private val enabled: Boolean = true
) : Printer {

    override fun println(message: String) {
        if (!enabled || !Trace.isEnabled()) return

        if (message.startsWith(LOOP_ENTER_PREFIX)) {
            Trace.beginSection(buildLabel(message, MAX_LABEL_LENGTH))
        } else if (message.startsWith(LOOP_EXIT_PREFIX)) {
            Trace.endSection()
        }
    }

}
