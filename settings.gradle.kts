rootProject.name = "tracing"

pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

include(":events")
include(":plugin")
include(":tracer")
