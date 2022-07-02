package io.johnsonlee.tracing.gradle

import io.johnsonlee.once.Once
import io.johnsonlee.tracing.event.DurationEvent
import io.johnsonlee.tracing.event.TraceEvent
import org.gradle.BuildAdapter
import org.gradle.BuildResult
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectEvaluationListener
import org.gradle.api.ProjectState
import org.gradle.api.Task
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.TaskInternal
import org.gradle.api.internal.artifacts.configurations.ConfigurationInternal
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState
import java.io.File

private const val CATEGORY_INITIALIZATION = "initialization"
private const val CATEGORY_CONFIGURATION = "configuration"
private const val CATEGORY_EXECUTION = "execution"

class GradleTracingPlugin : Plugin<Gradle> {

    private val once = Once<Boolean>()

    private val tracing = GradleTracingExtension()

    override fun apply(gradle: Gradle) {
        tracing.events.clear()
        once {
            tracing.events.add(TraceEvent(
                    name = "projects loading",
                    category = CATEGORY_INITIALIZATION,
                    phase = DurationEvent.BEGIN
            ))
            gradle.doApply(tracing)
            true
        }
    }

    private fun Gradle.doApply(tracing: GradleTracingExtension) {
        projectsLoaded {
            tracing.events.add(TraceEvent(
                    name = "projects loading",
                    category = CATEGORY_INITIALIZATION,
                    phase = DurationEvent.END
            ))
            tracing.events.add(TraceEvent(
                    name = "projects evaluation",
                    category = CATEGORY_CONFIGURATION,
                    phase = DurationEvent.BEGIN
            ))
        }
        projectsEvaluated {
            tracing.events.add(TraceEvent(
                    name = "projects evaluation",
                    category = CATEGORY_CONFIGURATION,
                    phase = DurationEvent.END
            ))
        }
        addListener(object : TaskExecutionListener {
            override fun beforeExecute(task: Task) {
                tracing.events.add(TraceEvent(
                        name = "${(task as TaskInternal).identityPath}",
                        category = CATEGORY_EXECUTION,
                        phase = DurationEvent.BEGIN
                ))
            }

            override fun afterExecute(task: Task, state: TaskState) {
                tracing.events.add(TraceEvent(
                        name = "${(task as TaskInternal).identityPath}",
                        category = CATEGORY_EXECUTION,
                        phase = DurationEvent.END,
                        args = mapOf("state" to mapOf(
                                "executed" to state.executed,
                                "skipped" to state.skipped,
                                "skipMessage" to state.skipMessage,
                                "didWork" to state.didWork,
                                "upToDate" to state.upToDate,
                                "failure" to state.failure,
                                "noSource" to state.noSource
                        ))
                ))
            }
        })
        addBuildListener(object : BuildAdapter() {
            val settings = Once<Boolean>()

            init {
                settings {
                    tracing.events.add(TraceEvent(
                            name = "settings evaluation",
                            category = CATEGORY_CONFIGURATION,
                            phase = DurationEvent.BEGIN
                    ))
                }
            }

            override fun beforeSettings(settings: Settings) {
                settings {
                    tracing.events.add(TraceEvent(
                            name = "settings evaluation",
                            category = CATEGORY_CONFIGURATION,
                            phase = DurationEvent.BEGIN
                    ))
                }
            }

            override fun settingsEvaluated(settings: Settings) {
                tracing.events.add(TraceEvent(
                        name = "settings evaluation",
                        category = CATEGORY_CONFIGURATION,
                        phase = DurationEvent.END
                ))
            }

            override fun buildFinished(result: BuildResult) {
                File(rootProject.buildDir, "trace.html").also {
                    if (it.exists()) {
                        it.delete()
                    }
                    it.createNewFile()
                }.outputStream().use { out ->
                    TraceGenerator.generate(out, "Trace for project ${rootProject.name}", tracing.events)
                }
            }
        })
        addProjectEvaluationListener(object : ProjectEvaluationListener {
            override fun beforeEvaluate(project: Project) {
                tracing.events.add(TraceEvent(
                        name = "${(project as ProjectInternal).identityPath} evaluation",
                        category = CATEGORY_CONFIGURATION,
                        phase = DurationEvent.BEGIN
                ))
            }

            override fun afterEvaluate(project: Project, state: ProjectState) {
                tracing.events.add(TraceEvent(
                        name = "${(project as ProjectInternal).identityPath} evaluation",
                        category = CATEGORY_CONFIGURATION,
                        phase = DurationEvent.END,
                        args = mapOf(
                                "state" to mapOf(
                                        "executed" to state.executed,
                                        "failure" to state.failure
                                )
                        )
                ))
            }
        })
        addListener(object : DependencyResolutionListener {
            override fun beforeResolve(dependencies: ResolvableDependencies) {
                tracing.events.add(TraceEvent(
                        name = "Resolving ${dependencies.identifyPath}",
                        category = CATEGORY_CONFIGURATION,
                        phase = DurationEvent.BEGIN
                ))
            }

            override fun afterResolve(dependencies: ResolvableDependencies) {
                tracing.events.add(TraceEvent(
                        name = "Resolving ${dependencies.identifyPath}",
                        category = CATEGORY_CONFIGURATION,
                        phase = DurationEvent.END
                ))
            }

        })
    }

}

private val ResolvableDependencies.identifyPath: String
    get() = (dependencies as? ConfigurationInternal)?.identityPath?.toString() ?: dependencies.toString()