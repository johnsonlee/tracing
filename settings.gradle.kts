rootProject.name = "tracing"

pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

include(":event")
include(":plugin")
include(":runtime")
include(":util")
