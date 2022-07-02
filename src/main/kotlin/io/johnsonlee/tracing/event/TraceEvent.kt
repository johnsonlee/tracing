package io.johnsonlee.tracing.event

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import java.lang.management.ManagementFactory

data class TraceEvent(
        @JsonProperty("name")
        val name: String,

        @JsonProperty("cat")
        val category: String,

        @JsonProperty("ph")
        val phase: EventPhase,

        @JsonProperty("pid")
        val pid: Long = ManagementFactory.getRuntimeMXBean().name.substringBefore('@').toLongOrNull() ?: 0,

        @JsonProperty("tid")
        val tid: Long = Thread.currentThread().id,

        @JsonProperty("ts")
        val ts: Long = System.currentTimeMillis(),

        @JsonProperty("args")
        val args: Map<String, Any?> = emptyMap()
) : Serializable


interface EventPhase : Serializable

enum class DurationEvent : EventPhase {
    @JsonProperty("B")
    BEGIN,

    @JsonProperty("E")
    END

}

enum class CompleteEvent : EventPhase {
    @JsonProperty("X")
    COMPLETE
}

enum class InstanceEvent : EventPhase {
    @JsonProperty("i")
    INSTANT,
}


enum class CounterEvent : EventPhase {
    @JsonProperty("C")
    COUNTER,
}

enum class AsyncEvent : EventPhase {
    @JsonProperty("b")
    NESTABLE_START,

    @JsonProperty("n")
    NESTABLE_INSTANT,

    @JsonProperty("e")
    NESTABLE_END,
}

enum class FlowEvent : EventPhase {
    @JsonProperty("s")
    START,

    @JsonProperty("t")
    STEP,

    @JsonProperty("f")
    END,
}

enum class SampleEvent : EventPhase {
    @JsonProperty("P")
    SAMPLE,
}
