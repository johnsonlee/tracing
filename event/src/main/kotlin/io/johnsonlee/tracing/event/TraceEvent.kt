package io.johnsonlee.tracing.event

import java.io.Serializable

interface TraceEvent : Serializable {
    val name: String
    val category: String
    val phase: EventPhase
    val ts: Long
    val tts: Long
    val pid: Long
    val tid: Long
    val args: Map<String, Any?>
}
