package io.johnsonlee.tracing.event

import com.fasterxml.jackson.annotation.JsonProperty

data class CompleteEvent @JvmOverloads constructor(
        @JsonProperty("name")
        override val name: String,

        @JsonProperty("cat")
        override val category: String,

        @JsonProperty("dur")
        val duration: Long,

        @JsonProperty("ts")
        override val ts: Long = ts(),

        @JsonProperty("tts")
        override val tts: Long = tts(),

        @JsonProperty("pid")
        override val pid: Long = pid(),

        @JsonProperty("tid")
        override val tid: Long = tid(),

        @JsonProperty("args")
        override val args: Map<String, Any?> = emptyMap()
) : TraceEvent {

    @JsonProperty("ph")
    override val phase: EventPhase = Complete.COMPLETE

}


