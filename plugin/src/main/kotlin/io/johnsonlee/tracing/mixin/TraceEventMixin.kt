package io.johnsonlee.tracing.mixin

import com.fasterxml.jackson.annotation.JsonProperty
import io.johnsonlee.tracing.event.EventPhase

abstract class TraceEventMixin {

    @get:JsonProperty("name")
    abstract val name: String

    @get:JsonProperty("cat")
    abstract val category: String

    @get:JsonProperty("dur")
    abstract val duration: Long

    @get:JsonProperty("ts")
    abstract val ts: Long

    @get:JsonProperty("tts")
    abstract val tts: Long

    @get:JsonProperty("pid")
    abstract val pid: Long

    @get:JsonProperty("tid")
    abstract val tid: Long

    @get:JsonProperty("args")
    abstract val args: Map<String, Any?>

    @get:JsonProperty("ph")
    abstract val phase: EventPhase

}
