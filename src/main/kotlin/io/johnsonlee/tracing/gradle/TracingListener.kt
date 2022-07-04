package io.johnsonlee.tracing.gradle

import com.sun.source.util.TaskListener
import io.johnsonlee.once.Once
import io.johnsonlee.tracing.Trace
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Describable
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
import org.gradle.api.internal.artifacts.transform.ArtifactTransformListener
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.internal.project.taskfactory.TaskIdentity
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestResult
import java.io.File

@Suppress("UnstableApiUsage", "OVERRIDE_DEPRECATION")
internal class TracingListener(
        private val gradle: Gradle
) : ArtifactTransformListener
  , BuildListener
  , DependencyResolutionListener
  , ProjectEvaluationListener
  , TaskExecutionListener
  , TaskListener
  , TestListener {

    init {
        gradle.addListener(this)
        Trace.begin(gradle.cmdline, CATEGORY_BUILD)
    }

    override fun buildStarted(gradle: Gradle) = Unit

    override fun beforeSettings(settings: Settings) {
        Trace.begin("settings evaluation", CATEGORY_INITIALIZATION)
    }

    override fun settingsEvaluated(settings: Settings) {
        Trace.end("settings evaluation", CATEGORY_INITIALIZATION)
        Trace.begin("projects loading", CATEGORY_CONFIGURATION)
    }

    override fun projectsLoaded(gradle: Gradle) {
        Trace.end("projects loading", CATEGORY_CONFIGURATION)
        Trace.begin("projects evaluation", CATEGORY_CONFIGURATION)
    }

    override fun projectsEvaluated(gradle: Gradle) {
        Trace.end("projects evaluation", CATEGORY_CONFIGURATION)
    }

    override fun buildFinished(result: BuildResult) {
        Trace.end(gradle.cmdline, CATEGORY_BUILD)
        val html = File(gradle.rootProject.buildDir, "trace.html").also {
            if (it.exists()) {
                it.delete()
            }
            it.createNewFile()
        }
        Trace.dump("Trace for project ${gradle.rootProject.name}", html)
    }

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

    fun beforeExecute(taskIdentity: TaskIdentity<*>) {
        Trace.begin(taskIdentity.identityPath.toString(), CATEGORY_EXECUTION)
    }

    fun afterExecute(taskIdentity: TaskIdentity<*>, state: TaskState) {
        Trace.end(taskIdentity.identityPath.toString(), CATEGORY_EXECUTION, args = mapOf(
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

    override fun beforeExecute(task: Task) {
        beforeExecute((task as TaskInternal).taskIdentity)
    }

    override fun afterExecute(task: Task, state: TaskState) {
        afterExecute((task as TaskInternal).taskIdentity, state)
    }

    override fun beforeResolve(dependencies: ResolvableDependencies) {
        Trace.begin("Resolving ${dependencies.identifyPath}", CATEGORY_CONFIGURATION)
    }

    override fun afterResolve(dependencies: ResolvableDependencies) {
        Trace.end("Resolving ${dependencies.identifyPath}", CATEGORY_CONFIGURATION)
    }

    override fun beforeTransformerInvocation(transformer: Describable, subject: Describable) {
        Trace.begin("${subject.displayName}(${transformer.displayName})", CATEGORY_EXECUTION)
    }

    override fun afterTransformerInvocation(transformer: Describable, subject: Describable) {
        Trace.end("${subject.displayName}(${transformer.displayName})", CATEGORY_EXECUTION)
    }

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
}

private const val CATEGORY_BUILD = "build"
private const val CATEGORY_INITIALIZATION = "initialization"
private const val CATEGORY_CONFIGURATION = "configuration"
private const val CATEGORY_EXECUTION = "execution"
private const val CATEGORY_TEST = "test"

private val Gradle.cmdline: String
    get() = startParameter.taskNames.joinToString(" ", "./gradlew ")

private val ResolvableDependencies.identifyPath: String
    get() = (dependencies as? ConfigurationInternal)?.identityPath?.toString() ?: dependencies.toString()
