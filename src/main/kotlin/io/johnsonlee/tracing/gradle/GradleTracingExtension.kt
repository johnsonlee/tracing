package io.johnsonlee.tracing.gradle

import io.johnsonlee.tracing.event.TraceEvent
import java.util.concurrent.CopyOnWriteArrayList

open class GradleTracingExtension {

    internal val events = CopyOnWriteArrayList<TraceEvent>()

}