package io.johnsonlee.tracing.event

data class CompleteEvent @JvmOverloads constructor(
        override val name: String,
        override val category: String,
        val duration: Long,
        override val ts: Long,
        override val tts: Long,
        override val pid: Long,
        override val tid: Long,
        override val args: Map<String, Any?> = emptyMap()
) : TraceEvent {

    override val phase: EventPhase = Complete.COMPLETE

}


