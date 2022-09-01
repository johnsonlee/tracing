package io.johnsonlee.tracing.runtime

import android.util.Printer
import androidx.tracing.Trace
import io.johnsonlee.tracing.util.LooperUtil.PREFIX_ENTER_LOOP
import io.johnsonlee.tracing.util.LooperUtil.PREFIX_EXIT_LOOP
import io.johnsonlee.tracing.util.LooperUtil.buildTraceLabel

/**
 * A [Printer] for looper tracing
 */
class LooperTracer : Printer {

    override fun println(message: String) {
        if (!Trace.isEnabled()) return

        if (message.startsWith(PREFIX_ENTER_LOOP)) {
            Trace.beginSection(buildTraceLabel(message))
        } else if (message.startsWith(PREFIX_EXIT_LOOP)) {
            Trace.endSection()
        }
    }

}