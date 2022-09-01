package io.johnsonlee.tracing.event

import java.io.Serializable

interface EventPhase : Serializable

enum class Duration : EventPhase { BEGIN, END }
enum class Complete : EventPhase { COMPLETE }
enum class Instance : EventPhase { INSTANT }
enum class Counter : EventPhase { COUNTER }
enum class Async : EventPhase { NESTABLE_START, NESTABLE_INSTANT, NESTABLE_END }
enum class Flow : EventPhase { START, STEP, END }
enum class Sample : EventPhase { SAMPLE }