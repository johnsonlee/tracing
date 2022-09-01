package io.johnsonlee.tracing.runtime

import android.os.SystemClock
import android.util.Log
import android.util.Printer
import io.johnsonlee.tracing.util.LooperUtil.PREFIX_ENTER_LOOP
import io.johnsonlee.tracing.util.LooperUtil.PREFIX_EXIT_LOOP
import io.johnsonlee.tracing.util.LooperUtil.findCallback
import io.johnsonlee.tracing.util.LooperUtil.findHandler
import io.johnsonlee.tracing.util.LooperUtil.findWhat
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.NANOSECONDS

private const val TAG = "LooperProfiler"

@JvmSynthetic
private val TIME_UNIT_SYMBOLS: Array<String> = arrayOf("ns", "μs", "ms", "s", "m", "h", "d")

/**
 * A [Printer] for looper profiling
 */
open class LooperProfiler(
    threshold: Long = 200L,
    timeUnit: TimeUnit = MILLISECONDS,
    private val callback: (Int, Long, String) -> Unit = { what, duration, message ->
        val log: (String, String) -> Int = if (duration >= NANOSECONDS.convert(threshold, timeUnit)) Log::w else Log::i
        log(TAG, "[${"%8d".format(timeUnit.convert(duration, NANOSECONDS))}${TIME_UNIT_SYMBOLS[timeUnit.ordinal]}] ${"%4d".format(what)} : ${findCallback(message) ?: findHandler(message) ?: message}")
    }
) : Printer {

    private var ts: Long = 0L
    private var what: Int = 0

    /**
     *  callback for each loop
     *
     *  @param message the message
     *  @param what the message id
     *  @param duration the duration of current loop in microseconds
     */
    protected open fun onEachLoop(what: Int, duration: Long, message: String) = callback(what, duration, message)

    final override fun println(message: String) {
        if (message.startsWith(PREFIX_ENTER_LOOP)) {
            ts = SystemClock.elapsedRealtimeNanos()
            what = findWhat(message)
        } else if (message.startsWith(PREFIX_EXIT_LOOP)) {
            onEachLoop(what, SystemClock.elapsedRealtimeNanos() - ts, message)
        }
    }

}