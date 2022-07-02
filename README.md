# Tracing

A Gradle plugin to generate trace files for Gradle projects

## Getting Started

### Install Init Script

```bash
curl -L https://raw.githubusercontent.com/johnsonlee/tracing/main/src/main/resources/init.gradle.kts > ~/.gradle/init.d/tracing.gradle.kts
```

### Execute Gradle Tasks

```bash
./gradlew assemble
```

Then, you can see the `trace.html` file in the `build` directory.

```bash
open build/trace.html
```


