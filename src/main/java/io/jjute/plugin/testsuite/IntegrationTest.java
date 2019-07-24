package io.jjute.plugin.testsuite;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;

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
public class IntegrationTest {

    /**
     * Determines whether the {@code Project} is a simple Java application project or a Java library project.
     * This designation will affect which base plugin is applied to the project.
     */
    private enum ProjectType {
        JAVA("java"), JAVA_LIBRARY("java-library");

        private final String id;
        ProjectType(String id) {
            this.id = id;
        }
    }
    /**
     * {@code Project} instance used to execute this integration test.
     */
    protected final Project project;

    private IntegrationTest() {
        project = createJavaProject();
    }

    /**
     * Create a new Gradle {@code Project} instance for this integration test.
     *
     * @param type {@code ProjectType} to create.
     * @return the newly created {@code Project} instance
     *
     * @throws GradlePluginTestException if an I/O exception occurred while creating the project root directory
     */
    private static Project createProject(ProjectType type) {

        try {
            File projectDir = Files.createTempDirectory("jute-plugin").toFile();
            Project project = ProjectBuilder.builder().withProjectDir(projectDir).build();
            project.getPluginManager().apply(type.id); return project;
        }
        catch (java.io.IOException e) {
            throw new GradlePluginTestException("Unable to create project root directory", e);
        }
    }
    /**
     * @return a new Java project for this integration test.
     */
    public static Project createJavaProject()  {
        return createProject(ProjectType.JAVA);
    }
    /**
     * @return a new Java library project for this integration test.
     */
    public static Project createJavaLibraryProject() {
        return createProject(ProjectType.JAVA_LIBRARY);
    }
    /**
     * @return the {@code Project} associated with this integration test.
     */
    public Project getProject() {
        return project;
    }
}
