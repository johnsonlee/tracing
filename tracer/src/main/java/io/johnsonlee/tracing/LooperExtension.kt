package io.johnsonlee.tracing

@JvmSynthetic
internal const val LOOP_ENTER_PREFIX = ">>>>> Dispatching to "

@JvmSynthetic
internal const val LOOP_EXIT_PREFIX = "<<<<< Finished to "

private const val HANDLER_PREFIX = "Handler ("
private const val DISPATCHED_CONTINUATION_PREFIX = "DispatchedContinuation["
private const val CONTINUATION_AT_PREFIX = ", Continuation at "

@JvmSynthetic
internal fun buildLabel(message: String, maxLength: Int = message.length): String {
    val label = when {
        message.startsWith(LOOP_ENTER_PREFIX) -> buildLabel(message, LOOP_ENTER_PREFIX, message.lastIndexOf(':'))
        message.startsWith(LOOP_EXIT_PREFIX) -> buildLabel(message, LOOP_EXIT_PREFIX, message.length)
        else -> null
    } ?: message
    return if (label.length > maxLength) label.take(maxLength) else label
}

private fun buildLabel(message: String, prefix: String, end: Int): String? {
    return buildHandlerLabel(message, prefix, end)
        ?: buildCoroutineLabel(message, prefix, end)
}

// example: Handler (android.app.ActivityThread$H) {123456} null: 1
// example: Handler (android.app.ActivityThread$H) {123456} android.view.ViewRootImpl$4@33e0fae: 1
private fun buildHandlerLabel(message: String, prefix: String, end: Int): String? {
    if (!message.startsWith(HANDLER_PREFIX, prefix.length))
        return null

    val lp = prefix.length + HANDLER_PREFIX.length
    val rp = message.indexOf(')', lp)
    val rb = message.indexOf('}', rp + 1)
    val dc = message.indexOf(DISPATCHED_CONTINUATION_PREFIX, rb.coerceAtLeast(rp.coerceAtLeast(lp)))
    if (dc > -1) {
        val at = message.indexOf(CONTINUATION_AT_PREFIX, dc + DISPATCHED_CONTINUATION_PREFIX.length)
        val rk = message.lastIndexOf(']').takeIf { it >= 0 } ?: end
        return message.substring(at + CONTINUATION_AT_PREFIX.length, rk)
    }

    if (rb > -1) {
        val offset = if (message[rb + 1] == ' ') 2 else 1
        val callback = message.substring(rb + offset, end)
        if ("null" != callback) {
            return callback
        }
    }

    if (rp > lp) {
        return message.substring(lp, rp)
    }

    return null
}

// example: ...DispatchedContinuation[Dispatchers.Main, Continuation at com.example.Example@123456]
// example: ...DispatchedContinuation[Dispatchers.Main.immediate, Continuation at com.example.Example@123456]
private fun buildCoroutineLabel(message: String, prefix: String, end: Int): String? {
    val dc = message.indexOf(DISPATCHED_CONTINUATION_PREFIX, prefix.length).takeIf { it >= 0 } ?: return null
    val at = message.indexOf(CONTINUATION_AT_PREFIX, dc + DISPATCHED_CONTINUATION_PREFIX.length).takeIf { it >= 0 } ?: return null
    val rk = message.lastIndexOf(']').takeIf { it >= 0 } ?: end
    return message.substring(at + CONTINUATION_AT_PREFIX.length, rk).substringBeforeLast('@')
}
