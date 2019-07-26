package io.jjute.plugin.testsuite;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.nio.file.Files;

/**
 * <p>
 *     This class represents a Gradle <b>integration</b> test.
 * <p>
 *     Integration testing verifies that multiple classes or components work together as a whole.
 *     The code under test may reach out to external subsystems. Integration tests in the context
 *     of this test suite are mainly interacting with {@code Project} instances as opposed to
 *     {@code Plugin} instances which are handled by {@code FunctionalTest}. An integration test
 *     should not need to create build or plugin instances since it is only testing how well
 *     a system integrates with {@code Project} instances. If you need a more robust testing
 *     frame that tests plugin execution then use {@code FunctionalTest}.
 * </p>
 * @see <a href="https://guides.gradle.org/testing-gradle-plugins/#integration-tests">
 *      Testing Gradle Plugins: Implementing integration tests</a>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntegrationTest {

    /**
     * {@code Project} instance used to execute this integration test.
     */
    protected final Project project;

    /**
     * @throws GradlePluginTestException if an I/O exception occurred while creating the project root directory
     */
    public IntegrationTest() {

        try {
            File projectDir = Files.createTempDirectory("jute-plugin").toFile();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try { FileUtils.deleteDirectory(projectDir); }
                catch (java.io.IOException e) {
                    throw new GradlePluginTestException("Failed to schedule " +
                            "project root dir deletion: \"%s\"", projectDir.getPath(), e);
                }
            }));
            this.project = ProjectBuilder.builder().withProjectDir(projectDir).build();
            this.project.getPluginManager().apply("java");
        }
        catch (java.io.IOException e) {
            throw new GradlePluginTestException("Unable to create project root directory", e);
        }
    }

    /**
     * @return the {@code Project} associated with this integration test.
     */
    public Project getProject() {
        return project;
    }
}
