package io.jjute.plugin.framework.config

import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestLoggingContainer

/**
 * This <code>ConfigRunnable</code> will apply configuration actions defined
 * in <code>run()</code> method to each given <code>Test</code> task instance
 * found in an array of <code>Test</code> tasks provided at construction time.
 * <ul>
 *     <li>Clean old test results before running new tests.
 *     <li>Enable or disable test execution stack trace logging.
 *     <li>Change stack trace detail level with execution exception format.
 *     <li>Enable or disable test <i>fail-fast</i> behavior (fail task on first failed test).
 *     <li>Provide <i>lifecycle</i> logs before each test instance is executed.
 * </ul>
 */
class ConfigureTest extends ConfigRunnable {

    private Test[] testsToConfigure

    ConfigureTest(ProjectConfigurator configurator, Test[] tests) {
        super(configurator)
        testsToConfigure = tests
    }

    @Override
    void run() {

        testsToConfigure.each { test ->

            // Clean old test-results first
            test.dependsOn("cleanTest")

            // Configure test execution logging
            TestLoggingContainer logging = test.getTestLogging()

            logging.exceptionFormat = config.testExceptionFormat
            logging.showStackTraces = config.testShowStackTraces

            test.failFast = config.testFailFast

            // Listen to events in the test execution lifecycle
            test.beforeTest { descriptor ->
                project.logger.lifecycle("Running test: $descriptor")
            }
            test.doLast {
                project.logger.quiet("Finished running Unit Tests.")
            }
        }
    }
}
