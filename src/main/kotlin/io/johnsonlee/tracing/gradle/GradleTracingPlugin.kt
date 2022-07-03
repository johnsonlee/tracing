package io.johnsonlee.tracing.gradle

import io.johnsonlee.tracing.Trace
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectEvaluationListener
import org.gradle.api.ProjectState
import org.gradle.api.Task
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.internal.TaskInternal
import org.gradle.api.internal.artifacts.configurations.ConfigurationInternal
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestResult
import java.io.File

private const val CATEGORY_BUILD = "build"
private const val CATEGORY_INITIALIZATION = "initialization"
private const val CATEGORY_CONFIGURATION = "configuration"
private const val CATEGORY_EXECUTION = "execution"
private const val CATEGORY_TEST = "test"

class GradleTracingPlugin : Plugin<Gradle> {

    override fun apply(gradle: Gradle) {
        gradle.doApply()
    }

    @Suppress("UnstableApiUsage")
    private fun Gradle.doApply() {
        buildStarted {
            Trace.begin(gradle.cmdline, CATEGORY_BUILD)
        }
        beforeSettings {
            Trace.begin("settings evaluation", CATEGORY_INITIALIZATION)
            it.gradle.doApply()
        }
        settingsEvaluated {
            Trace.end("settings evaluation", CATEGORY_INITIALIZATION)
            Trace.begin("projects loading", CATEGORY_CONFIGURATION)
        }
        projectsLoaded {
            Trace.end("projects loading", CATEGORY_CONFIGURATION)
            Trace.begin("projects evaluation", CATEGORY_CONFIGURATION)
        }
        projectsEvaluated {
            Trace.end("projects evaluation", CATEGORY_CONFIGURATION)
        }
        buildFinished {
            Trace.end(cmdline, CATEGORY_BUILD)
            val html = File(rootProject.buildDir, "trace.html").also {
                if (it.exists()) {
                    it.delete()
                }
                it.createNewFile()
            }
            Trace.dump("Trace for project ${rootProject.name}", html)
        }
        addListener(object : TaskExecutionListener {
            override fun beforeExecute(task: Task) {
                Trace.begin((task as TaskInternal).identityPath.toString(), CATEGORY_EXECUTION)
            }

            override fun afterExecute(task: Task, state: TaskState) {
                Trace.end((task as TaskInternal).identityPath.toString(), CATEGORY_EXECUTION, args = mapOf(
                        "state" to mapOf(
                                "executed" to state.executed,
                                "skipped" to state.skipped,
                                "skipMessage" to state.skipMessage,
                                "didWork" to state.didWork,
                                "upToDate" to state.upToDate,
                                "failure" to state.failure,
                                "noSource" to state.noSource
                        )))
            }
        })
        addListener(object : ProjectEvaluationListener {
            override fun beforeEvaluate(project: Project) {
                Trace.begin("${(project as ProjectInternal).identityPath} evaluation", CATEGORY_CONFIGURATION)
            }

            override fun afterEvaluate(project: Project, state: ProjectState) {
                Trace.end("${(project as ProjectInternal).identityPath} evaluation", CATEGORY_CONFIGURATION, args = mapOf(
                        "state" to mapOf(
                                "executed" to state.executed,
                                "failure" to state.failure
                        )
                ))
            }
        })
        addListener(object : DependencyResolutionListener {

            override fun beforeResolve(dependencies: ResolvableDependencies) {
                Trace.begin("Resolving ${dependencies.identifyPath}", CATEGORY_CONFIGURATION)
            }

            override fun afterResolve(dependencies: ResolvableDependencies) {
                Trace.end("Resolving ${dependencies.identifyPath}", CATEGORY_CONFIGURATION)
            }

        })
        addListener(object : TestListener {
            override fun beforeSuite(suite: TestDescriptor) {
                Trace.begin(suite.displayName, CATEGORY_TEST)
            }

            override fun afterSuite(suite: TestDescriptor, result: TestResult) {
                Trace.end(suite.displayName, CATEGORY_TEST)
            }

            override fun beforeTest(testDescriptor: TestDescriptor) {
                Trace.begin(testDescriptor.displayName, CATEGORY_TEST)
            }

            override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
                Trace.end(testDescriptor.displayName, CATEGORY_TEST)
            }

        })
    }

}

private val Gradle.cmdline: String
    get() = startParameter.taskNames.joinToString(" ", "./gradlew ")

private val ResolvableDependencies.identifyPath: String
    get() = (dependencies as? ConfigurationInternal)?.identityPath?.toString() ?: dependencies.toString()