package io.jjute.plugin.testsuite;

import io.jjute.plugin.framework.PluginUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.logging.Logger;
import org.gradle.testkit.runner.GradleRunner;

import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * <p>
 *     This class represents a Gradle <b>functional</b> test.
 * <p>
 *     Functional testing is used to test the system from the end user’s perspective.
 *     End-to-end tests for Gradle plugins stand up a build script, apply the plugin under test
 *     and  execute the build with a specific task. The outcome of the build (e.g. standard
 *     output/error or generated artifacts) verifies the correctness of the functionality.
 * </p>
 * @see <a href="https://guides.gradle.org/testing-gradle-plugins/#functional-tests">
 *      Testing Gradle Plugins: Implementing functional tests</a>
 */
public class FunctionalTest {

    /**
     * Instance of {@code GradleRunner} used to build and execute tests.
     */
    protected final JuteGradleRunner buildRunner;

    /**
     * Plugin identifier used to resolve the plugin in the test build.
     */
    protected final String pluginId;

    /**
     * The directory that Gradle will be executed in
     * which is also the root project of the build under test.
     */
    protected final File buildDir;

    /**
     * The {@code build.gradle} file being tested.
     * It will always be located in the root project directory - {@link #buildDir}.
     */
    protected final File buildFile;

    /**
     * Prepare project build test area in a new directory located in the
     * system designated default temporary-file directory. The test area
     * will be cleaned after the JVM that invoked the tests terminates.
     *
     * @throws GradlePluginTestException if an {@code IOException} occurred while trying to create or
     *                                   schedule deletion of the root directory or build file.
     */
    public FunctionalTest() {

        try {
            buildDir = Files.createTempDirectory("jute-plugin").toFile();
        }
        catch (java.io.IOException e) {
            throw new GradlePluginTestException("Unable to create build root directory", e);
        }

        buildFile = buildDir.toPath().resolve("build.gradle").toFile();

        try {
            if (!buildFile.createNewFile()) {
                throw new java.nio.file.FileAlreadyExistsException("Project build file already exists.");
            }
        } catch (java.io.IOException e) {
            throw new GradlePluginTestException("Unable to create " +
                    "test project build file: \"%s\"", buildFile.getPath(), e);
        } try {
            FileUtils.forceDeleteOnExit(buildDir);
        }
        catch (java.io.IOException e) {
           throw new GradlePluginTestException("Failed to schedule " +
                   "build root dir deletion: \"%s\"", buildDir.getPath(), e);
        }

        buildRunner = createRunnerForPlugin();

        try {
            pluginId = getPluginIdentifier();
        }
        catch (IOException e) {
            throw new GradlePluginTestException("Unable to resolve plugin identifier.", e);
        }
    }

    /**
     * Internal method that does the actual writing to file.
     *
     * @param text array of text lines to write to file. {@code null} entries produce blank lines.
     * @param append if {@code true}, the lines will be added to the end of the file rather than overwriting
     *
     * @throws GradlePluginTestException if an I/O exception occurred while writing to build file.
     */
    private void writeToBuildFile(String[] text, boolean append) {

        try {
            java.util.List<String> toWrite = new ArrayList<>(java.util.Arrays.asList(text));
            FileUtils.writeLines(buildFile, toWrite, append);
        }
        catch (java.io.IOException e) {
            throw new GradlePluginTestException("An I/O exception occurred while writing to build file.", e);
        }
    }

    /**
     * Writes the {@code toString()} value of each array item to the build {@code File} line by line.
     * The lines are added to the end of the file rather then overwriting existing content.
     *
     * @param text array of text lines to write to file. {@code null} entries produce blank lines.
     */
    protected void writeToBuildFile(String[] text) {
        writeToBuildFile(text, true);
    }

    /**
     * <p>
     *     Initialize the test build file by delete existing content <i>(if any)</i> and
     *     declare and apply the plugin being tested with {@code plugins} DSL code block.
     * <p>
     *     This method should be called before each test that plans to write to build file,
     *     preferably in a dedicated JUnit method annotated with {@code @BeforeEach}.
     *     Alternatively you can call {@link #initAndWriteToBuildFile(String[])}
     *     method to write the initial text in each test method.
     */
    protected void initializeBuildFile() {

        String[] plugins = { "plugins {", String.format("id '%s'", pluginId), "}\n" };
        writeToBuildFile(plugins, false);
    }

    /**
     * Initialize the test build file by delete existing content and write the
     * {@code toString()} value of each array item to the build {@code File} line by line.
     *
     * @param text array of text lines to write to file. {@code null} entries produce blank lines.
     *
     * @see #initializeBuildFile()
     * @see #writeToBuildFile(String[])
     */
    protected void initAndWriteToBuildFile(String[] text) {
        initializeBuildFile(); writeToBuildFile(text);
    }

    /**
     * @return a new instance of {@code JuteGradleRunner} configured for use.
     */
    protected JuteGradleRunner createRunnerForPlugin() {

        GradleRunner runner = JuteGradleRunner.create().withPluginClasspath();
        return (JuteGradleRunner) runner.withProjectDir(buildFile.getParentFile());
    }

    /**
     * Retrieve the plugin identifier as defined by the name of the properties file located in a
     * {@code META-INF/gradle-plugins} directory found on the plugin classpath. Gradle uses this
     * file to determine which class implements the Plugin interface. The name of this properties
     * file excluding the {@code .properties} extension becomes the identifier of the plugin.
     *
     * @return the fully qualified plugin identifier. This method will always
     *         return a valid {@code String} value (not {@code null} or empty).
     *
     * @throws IOException if an I/O error is thrown when accessing a classpath directory.
     * @throws GradlePluginTestException if plugin descriptor was not found on classpath. This will happen
     *                                   when using {@code java-gradle-plugin}, modifying package or class
     *                                   names and not re-running relevant Gradle tasks located under the
     *                                   {@code plugin development} group or when the plugin implementation
     *                                   class was not properly disclosed on the classpath.
     *
     * @see <a href="https://guides.gradle.org/writing-gradle-plugins/#declare_a_plugin_identifier">
     *      Writing Gradle Plugins: Declare a plugin identifier</a>
     */
    protected @NotEmpty String getPluginIdentifier() throws IOException {

        java.nio.file.Path path = java.nio.file.Paths.get("META-INF/gradle-plugins");
        File classpathDir = PluginUtils.findClasspathEntry(buildRunner.getPluginClasspath(), path);
        if (classpathDir == null) {
            throw new GradlePluginTestException("Unable to find \"META-INF/gradle-plugins\" on classpath.");
        }

        File[] propertiesFiles = classpathDir.listFiles(f ->
                FilenameUtils.getExtension(f.getName()).equals("properties"));

        if (propertiesFiles == null || propertiesFiles.length == 0) {
            throw new GradlePluginTestException("Unable to find plugin descriptor on classpath.");
        }
        else if (propertiesFiles.length > 1) {
            // TODO: Print warning here using a logger
        }
        return FilenameUtils.removeExtension(propertiesFiles[0].getName());
    }
}