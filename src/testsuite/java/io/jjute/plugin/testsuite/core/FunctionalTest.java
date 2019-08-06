package io.jjute.plugin.testsuite.core;

import io.jjute.plugin.framework.CommunityPlugin;
import io.jjute.plugin.framework.GradleProperties;
import io.jjute.plugin.framework.util.PluginUtils;
import io.jjute.plugin.testsuite.file.BuildFile;
import io.jjute.plugin.testsuite.JuteGradleRunner;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Project;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.TestInstance;

import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.InvalidPropertiesFormatException;

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
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public abstract class FunctionalTest extends PluginTest {

    protected final GradleProperties properties;

    /**
     * Instance of {@code GradleRunner} used to build and execute tests.
     */
    protected final JuteGradleRunner buildRunner;

    /**
     * @throws PluginTestException if {@code gradle.properties} resource file was not found in root
     *                                   directory, an I/O error occurred while finding resource or creating
     *                                   or scheduling the deletion of root directory or build file.
     */
    protected FunctionalTest() {
        super(Type.FUNCTIONAL);

        buildRunner = createRunnerForPlugin();
        try {
            properties = GradleProperties.getFromResources();
            if (properties != null) {
                properties.storeToFile(buildDir.toPath().resolve("gradle.properties").toFile());
            }
            else throw new PluginTestException("Unable to find gradle.properties resource file.");
        }
        catch (IOException e) {
            throw new PluginTestException("An I/O exception occurred while reading properties.");
        }
    }

    /**
     * Initialize {@code BuildFile} by deleting existing content, applying {@code JutePlugin}
     * and writing {@code toString()} value of each array item to build file line by line.
     *
     * @param text array of text lines to write to file. {@code null} entries produce blank lines.
     * @throws PluginTestException if the method is unable to resolve plugin identifier.
     */
    protected BuildFile initializeBuild(String[] text) {
        return BuildFile.create(buildDir).applyPlugins(getDevelopingPlugin()).write(text).sign();
    }

    /**
     * Write Gradle properties stored in given {@code Map} to {@link Project#GRADLE_PROPERTIES gradle.properties}
     * file located in the test project root directory. If the file does not exist it will be created.
     * Note that the properties however will not be written if the properties file could not be found.
     *
     * @param properties {@code Map} containing Gradle properties where map keys represent property names
     *                   <i>(left-hand side)</i> and map values represent property values <i>(right-hand side)</i>.
     *
     * @throws IOException if an I/O error occurred while writing properties to file.
     */
    protected void writeToGradleProperties(java.util.Map<String, Object> properties) throws IOException {

        StringBuilder sb = new StringBuilder();
        for (java.util.Map.Entry<String, Object> entry : properties.entrySet()) {
            sb.append(entry.getKey()).append('=').append(entry.getValue()).append("\n");
        }
        FileUtils.write(getGradleProperties(), sb.toString(), Charset.defaultCharset(), true);
    }

    /**
     * Write Gradle properties stored in given array to {@link Project#GRADLE_PROPERTIES  gradle.properties}
     * file located in the test project root directory by adding them to the end of the file <i>(appending)</i>.
     * If the file does not exist it will be created. Each array element will be interpreted and written to
     * file <i>as-is</i>, meaning each element is expected to be formatted according to the following syntax:
     * <    blockquote>
     * <dl>
     *     <dt><b>Syntax:</b>
     *         <dd>{@code <property-key>=<property-value>}
     *     <dt><b>Example:</b>
     *         <dd>{@code propertyKey=propertyValue}
     * </dl>
     * </blockquote>
     * @param properties array of text lines to write
     *
     * @throws IOException if an I/O error occurred while writing properties to file.
     * @throws InvalidPropertiesFormatException if any of the given properties is not a valid property.
     */
    protected void writeToGradleProperties(String[] properties) throws IOException {

        for (String property : properties) {
            if (property.split("=").length != 2)
                throw new InvalidPropertiesFormatException("\"" + property + "\" is not a valid property.");
        }
        FileUtils.writeLines(getGradleProperties(), java.util.Arrays.asList(properties), true);
    }

    /**
     * @return a {@code File} reference to {@link Project#GRADLE_PROPERTIES gradle.properties} located
     *         in the test project root directory. This method only returns an object representing the
     *         path to properties file and does not guarantee that the file actually exists on disk.
     */
    protected File getGradleProperties() {
        return buildDir.toPath().resolve("gradle.properties").toFile();
    }

    /**
     * Return a new instance of {@code JuteGradleRunner} configured for use by test methods.
     *
     * @param forwardOutput whether the output of executed builds should be forwarded to the
     *                      {@code System.out} stream. This is disabled by default as output
     *                      is always available via {@link BuildResult#getOutput()}.
     *
     * @param withDebug whether debugging support is enabled. If debug support is not enabled, the
     *                  build will be executed in an entirely separate process. This means that any
     *                  debugger that is attached to the test execution process will not be attached to
     *                  the build process. When debug support is enabled, the build is executed in the
     *                  same process that is using the Gradle Runner, allowing the build to be debugged.
     *                  Debug support is off (i.e. {@code false}) by default.
     */
    protected JuteGradleRunner createRunnerForPlugin(boolean forwardOutput, boolean withDebug) {

        final GradleRunner runner = JuteGradleRunner.create().withPluginClasspath()
                .withProjectDir(buildDir).withDebug(withDebug);

        if (forwardOutput) runner.forwardOutput();
        return (JuteGradleRunner) runner;
    }
    /**
     * Helper method to return a new instance of {@code JuteGradleRunner}
     * with <i>output forwarding</i> and <i>debug support</i> enabled.
     *
     * @see #createRunnerForPlugin(boolean, boolean)
     */
    protected JuteGradleRunner createRunnerForPlugin() {
        return createRunnerForPlugin(true, true);
    }

    /**
     * Resolve and return a {@code CommunityPlugin} reference that represents a plugin managed by Gradle Plugin
     * Development Plugin with an identifier that matches the plugin identifier as defined by the name of the
     * properties file located in {@code META-INF/gradle-plugins} directory found on the plugin classpath.
     *
     * @throws PluginTestException if the method is unable to resolve plugin identifier.
     * @see <a href="https://docs.gradle.org/current/userguide/java_gradle_plugin.html#java_gradle_plugin">
     *      Gradle Docs: Gradle Plugin Development Plugin</a>
     * @see #getPluginIdentifier()
     */
    protected CommunityPlugin getDevelopingPlugin() {

        try {
            return new CommunityPlugin(getPluginIdentifier());
        }
        catch (IOException e) {
            throw new PluginTestException("Unable to resolve plugin identifier.", e);
        }
    }

    /**
     * Retrieve the plugin identifier as defined by the name of the properties file located in
     * {@code META-INF/gradle-plugins} directory found on the plugin classpath. Gradle uses this
     * file to determine which class implements the Plugin interface. The name of this properties
     * file excluding the {@code .properties} extension becomes the identifier of the plugin.
     *
     * @return the fully qualified plugin identifier. This method will always
     *         return a valid {@code String} value (not {@code null} or empty).
     *
     * @throws IOException if an I/O error is thrown when accessing a classpath directory.
     * @throws PluginTestException if plugin descriptor was not found on classpath. This will happen
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
            throw new PluginTestException("Unable to find \"META-INF/gradle-plugins\" on classpath.");
        }

        File[] propertiesFiles = classpathDir.listFiles(f ->
                FilenameUtils.getExtension(f.getName()).equals("properties"));

        if (propertiesFiles == null || propertiesFiles.length == 0) {
            throw new PluginTestException("Unable to find plugin descriptor on classpath.");
        }
        else if (propertiesFiles.length > 1) {
            System.out.println("Warning: Multiple properties files detected in META-INF/gradle-plugins.");
        }
        return FilenameUtils.removeExtension(propertiesFiles[0].getName());
    }

    /**
     * Copy a resource {@code File} found under given path to a designated directory.
     *
     * @param resourcePath {@code String} representing a path to the target resource.
     * @param dirPath {@code String} representing destination directory path.
     * @return a reference to the successfully copied {@code File}.
     *
     * @throws NullPointerException if the resource under the given path was not found.
     * @throws PluginTestException if an I/O error occurred while copying file to directory.
     */
    protected File copyResourceToDirectory(String resourcePath, String dirPath) {

        try {
            java.net.URL url = getClass().getResource('/' + resourcePath);
            File resourceFile = new File(java.util.Objects.requireNonNull(url).toURI());
            File copyResultFile = buildDir.toPath().resolve(dirPath).toFile();
            FileUtils.copyFileToDirectory(resourceFile, copyResultFile);
            return copyResultFile.toPath().resolve(resourceFile.getName()).toFile();
        }
        catch (URISyntaxException | IOException e) {
            throw new PluginTestException(String.format("Unable to move resource " +
                    "\"%s\" to directory \"%s\"", resourcePath, dirPath), e);
        }
    }
}
