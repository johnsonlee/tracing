package io.johnsonlee.tracing.runtime

import android.os.SystemClock
import android.util.Printer
import io.johnsonlee.tracing.util.LooperUtil.PREFIX_ENTER_LOOP
import io.johnsonlee.tracing.util.LooperUtil.PREFIX_EXIT_LOOP
import io.johnsonlee.tracing.util.LooperUtil.buildStackTrace
import io.johnsonlee.tracing.util.LooperUtil.findCallback
import io.johnsonlee.tracing.util.LooperUtil.findHandler
import io.johnsonlee.tracing.util.LooperUtil.findWhat
import io.johnsonlee.tracing.util.LooperUtil.identity
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MICROSECONDS
import java.util.concurrent.TimeUnit.NANOSECONDS

/**
 * A [Printer] for looper profiling
 */
abstract class LooperProfiler : Printer {

    private var ts: Long = 0L
    private var what: Int = 0

    abstract fun onEachLoop(
            message: String,
            what: Int,
            duration: Long,
            timeUnit: TimeUnit,
            stackTrace: () -> Array<StackTraceElement>,
            callback: () -> String,
            handler: () -> String?
    )

    override fun println(message: String) {
        if (message.startsWith(PREFIX_ENTER_LOOP)) {
            ts = SystemClock.elapsedRealtimeNanos()
            what = findWhat(message)
        } else if (message.startsWith(PREFIX_EXIT_LOOP)) {
            val duration = NANOSECONDS.toMicros(SystemClock.elapsedRealtimeNanos() - ts)
            onEachLoop(message, what, duration, MICROSECONDS, {
                buildStackTrace(message)
            }, {
                findCallback(message) ?: identity(message)
            }, {
                findHandler(message)
            })
        }
    }

}