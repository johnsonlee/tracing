package io.johnsonlee.tracing.event

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

interface EventPhase : Serializable

enum class Duration : EventPhase {
    @JsonProperty("B")
    BEGIN,

    @JsonProperty("E")
    END

}

enum class Complete : EventPhase {
    @JsonProperty("X")
    COMPLETE
}

enum class Instance : EventPhase {
    @JsonProperty("i")
    INSTANT,
}


enum class Counter : EventPhase {
    @JsonProperty("C")
    COUNTER,
}

enum class Async : EventPhase {
    @JsonProperty("b")
    NESTABLE_START,

    @JsonProperty("n")
    NESTABLE_INSTANT,

    @JsonProperty("e")
    NESTABLE_END,
}

enum class Flow : EventPhase {
    @JsonProperty("s")
    START,

    @JsonProperty("t")
    STEP,

    @JsonProperty("f")
    END,
}

enum class Sample : EventPhase {
    @JsonProperty("P")
    SAMPLE,
}