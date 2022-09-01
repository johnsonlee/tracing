package io.johnsonlee.tracing.mixin

import com.fasterxml.jackson.annotation.JsonProperty
import io.johnsonlee.tracing.event.EventPhase

abstract class DurationMixin {
    @get:JsonProperty("B")
    abstract val BEGIN: EventPhase

    @get:JsonProperty("E")
    abstract val END: EventPhase
}

abstract class CompleteMixin {
    @get:JsonProperty("X")
    abstract val COMPLETE: EventPhase
}

abstract class InstanceMixin {
    @get:JsonProperty("i")
    abstract val INSTANT: EventPhase
}


abstract class CounterMixin {
    @get:JsonProperty("C")
    abstract val COUNTER: EventPhase
}

abstract class AsyncMixin {
    @get:JsonProperty("b")
    abstract val NESTABLE_START: EventPhase

    @get:JsonProperty("n")
    abstract val NESTABLE_INSTANT: EventPhase

    @get:JsonProperty("e")
    abstract val NESTABLE_END: EventPhase
}

abstract class FlowMixin {
    @get:JsonProperty("s")
    abstract val START: EventPhase

    @get:JsonProperty("t")
    abstract val STEP: EventPhase

    @get:JsonProperty("f")
    abstract val END: EventPhase
}

abstract class SampleMixin {
    @get:JsonProperty("P")
    abstract val SAMPLE: EventPhase
}