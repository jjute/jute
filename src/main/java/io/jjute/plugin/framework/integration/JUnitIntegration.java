package io.jjute.plugin.framework.integration;

import io.jjute.plugin.framework.util.ProjectUtils;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.testing.Test;

public class JUnitIntegration extends IntegrationModel {

    /**
     * This dependency allows us to write tests and extensions which use JUnit 5
     * @see <a href="https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api">
     *      Artifact on Maven Repository</a>
     */
    public static final String API = "org.junit.jupiter:junit-jupiter-api:5.5.0";

    /**
     * This dependency allows us to run tests which use JUnit 5
     * @see <a href="https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-engine">
     *      Artifact on Maven Repository</a>
     */
    public static final String ENGINE = "org.junit.jupiter:junit-jupiter-engine:5.5.0";

    private final Test test;

    public JUnitIntegration(Project project) {
        super("JUnit", project);
        test = ProjectUtils.getTestTask(project);
    }

    /**
     * Add JUnit {@code api} and {@code engine} dependencies to the associated project.
     */
    public void addProjectDependencies() {

        DependencyHandler dependencies = project.getDependencies();

        dependencies.add(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, API);
        dependencies.add(JavaPlugin.TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME, ENGINE);
    }

    /**
     * Gradle has native support for JUnit platforms which needs to be activated by
     * specifying that JUnit Platform (a.k.a. JUnit 5) should be used to execute the tests.
     * When a specific platform was not specified Gradle will detect the one we're using
     * presumably through the dependency declarations.
     */
    public void enableNativeSupport() {
        test.useJUnitPlatform();
    }
}
