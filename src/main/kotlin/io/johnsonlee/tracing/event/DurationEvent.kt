package io.johnsonlee.tracing.event

import com.fasterxml.jackson.annotation.JsonProperty

data class DurationEvent @JvmOverloads constructor(
        @JsonProperty("name")
        override val name: String,

        @JsonProperty("cat")
        override val category: String,

        @JsonProperty("ph")
        override val phase: EventPhase,

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

    companion object {

        fun begin(
                name: String,
                category: String,
                args: Map<String, Any?> = emptyMap()
        ) = DurationEvent(name, category, Duration.BEGIN, args = args)

        fun end(
                name: String,
                category: String,
                args: Map<String, Any?> = emptyMap()
        ) = DurationEvent(name, category, Duration.END, args = args)

    }

}


