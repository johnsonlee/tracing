package io.johnsonlee.tracing.event

data class DurationEvent @JvmOverloads constructor(
        override val name: String,
        override val category: String,
        override val phase: EventPhase,
        override val ts: Long,
        override val tts: Long,
        override val pid: Long,
        override val tid: Long,
        override val args: Map<String, Any?> = emptyMap()
) : TraceEvent
