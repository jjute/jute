package io.jjute.plugin.testsuite;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.internal.DefaultGradleRunner;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.List;

/**
 * <p>
 *     Executes a Gradle build, allowing inspection of the outcome.
 * <p>
 *     A Gradle runner can be used to functionally test build logic, by executing a contrived build.
 *     Assertions can then be made on the outcome of the build, such as the state of files created by
 *     the build, or what tasks were actually executed during the build.
 * <p>
 *     A runner can be created via the {@link #create()} method.
 * <p>
 *     Typically, the test code using the runner will programmatically create a build (e.g. by writing
 *     Gradle build files to a temporary space) to execute. The build to execute is effectively specified
 *     by the {@link #withProjectDir(File)} method. It is a requirement that a project directory be set.
 * <p>
 *     The {@link #withArguments(String...)} method allows the build arguments
 *     to be specified, just as they would be on the command line.
 * <p>
 *     The {@link #build()} method can be used to invoke the build when it is expected to succeed,
 *     while the {@link #buildAndFail()} method can be used when the build is expected to fail.
 * <p>
 *     GradleRunner instances are not thread safe and cannot be used concurrently.
 *     However, multiple instances are able to be used concurrently.
 * <p>
 *     Please see the Gradle <a href="https://docs.gradle.org/current/userguide/test_kit.html">
 *     TestKit</a>User Manual chapter for more information.
 */
public class JuteGradleRunner extends DefaultGradleRunner {

    private List<GradleProperty> userProperties = Collections.emptyList();

    /**
     * <p>
     *     Creates a new Gradle runner.
     * <p>
     *     The runner requires a Gradle distribution (and therefore a specific version of Gradle) in order
     *     to execute builds. This method will find a Gradle distribution, based on the filesystem location
     *     of this class. That is, it is expected that this class is loaded from a Gradle distribution.
     * <p>
     *     When using the runner as part of tests being executed by Gradle (i.e. a build
     *     using the {@code gradleTestKit()} dependency), the same distribution of Gradle
     *     that is executing the tests will be used by runner returned by this method.
     * <p>
     *     When using the runner as part of tests being executed by an IDE, the same
     *     distribution of Gradle that was used when importing the project will be used.
     * </p>
     */
    public static JuteGradleRunner create() {
        return new JuteGradleRunner();
    }
    /**
     * Creates a new Gradle runner.
     *
     * @param buildFile root build file of the project being tested
     * @param defaultClasspath use the plugin classpath based on the Gradle plugin development plugin
     *                         conventions. This will replace any previous classpath specified via
     *                         {@link #withPluginClasspath(Iterable)} and vice versa.
     * @see #create()
     */
    @Contract("_, _ -> new")
    public static JuteGradleRunner create(@NotNull java.io.File buildFile, boolean defaultClasspath) {

        GradleRunner runner = new JuteGradleRunner();
        runner = runner.withProjectDir(buildFile.getParentFile());
        return (JuteGradleRunner) (defaultClasspath ? runner.withPluginClasspath() : runner);
    }

    /**
     * Save the user properties as an unmodifiable view of the given list and add them to build arguments.
     */
    private JuteGradleRunner saveProperties(List<GradleProperty> properties) {
        userProperties = Collections.unmodifiableList(properties);
        return this;
    }
    /**
     * Save the given user property as an immutable singleton list and add it to build arguments.
     */
    private JuteGradleRunner saveProperty(GradleProperty property) {
        userProperties = Collections.singletonList(property);
        return this;
    }

    /**
     * Add a collection of properties to build arguments.
     *
     * @param properties a {@code Map} containing property values with keys representing
     *                   property names and values representing property values.
     */
    @Contract("_ -> this")
    public JuteGradleRunner withProperties(Map<String, String> properties) {

        List<String> arguments = new java.util.ArrayList<>();
        List<GradleProperty> tempProps = new java.util.ArrayList<>();

        for (Map.Entry<String, String> property : properties.entrySet()) {
            tempProps.add(GradleProperty.Type.PROJECT.create(property.getKey(), property.getValue()));
        }
        return saveProperties(tempProps);
    }

    /**
     * Add a single project property to build arguments.
     *
     * @param name property name <i>(left-side value)</i>
     * @param value property value <i>(right-side value)</i>
     */
    @Contract("_, _ -> this")
    public JuteGradleRunner withProperty(String name, String value) {
        return saveProperty(GradleProperty.Type.PROJECT.create(name, value));
    }

    /**
     * Add a single project property with multiple values to build arguments.
     *
     * @param name name of the property
     * @param values array of property values
     */
    @Contract("_, _ -> this")
    public JuteGradleRunner withProperty(String name, String[] values) {
        return withProperty(name, String.join(",", values));
    }

    /**
     * Add a single project property with multiple values to build arguments.
     *
     * @param name name of the property
     * @param values collection of property values
     *
     * @see #withProperty(String, String[])
     */
    @Contract("_, _ -> this")
    public JuteGradleRunner withProperty(String name, java.util.Collection<String> values) {
        return withProperty(name, values.toArray(new String[0]));
    }

    /**
     * @return project properties that were programmatically set by this instance of {@code JuteGradleRunner}.
     *         This will not include pre-defined properties or properties that were defined in other ways.
     *         Note that the returned list is <b>immutable</b>.
     */
    public List<GradleProperty> getUserProperties() {
        return userProperties;
    }

    /**
     * Run these operations and then build the runner:
     * <ul>
     *     <li>Inject all user properties as arguments.
     *     <li>Disable {@code help} task from running for a cleaner output.</li>
     *     <li>Enable {@code --stacktrace} option for easier debugging.</li>
     * </ul>
     * @param fail whether this build is expected to complete with failiure. If set to {@code true} then
     *             the method will run and return the result of {@link #buildAndFail()}, otherwise
     *             run and return the result of {@link #build()}.
     */
    private BuildResult prepareAndBuild(boolean fail) {

        final String[] properties = new String[userProperties.size()];
        for (int i = 0; i < userProperties.size(); i++) {
            properties[i] = userProperties.get(i).asArgument();
        }
        List<String> arguments = new java.util.ArrayList<>(getArguments());
        arguments.addAll(java.util.Arrays.asList(properties));
        /*
         * By default GradleRunner runs 'help' task before any other task.
         * This clutters the standard build output and is generally not desirable.
         */
        arguments.add("-xhelp");
        /*
         * This tells Gradle to print a stacktrace in case the plugin fails with an exception.
         * Without this we have no idea where or why the exception occurred.
         */
        arguments.add("--stacktrace");

        super.withArguments(arguments);
        return fail ? super.buildAndFail() : super.build();
    }

    @Override
    public BuildResult build() {
        return prepareAndBuild(false);
    }

    @Override
    public BuildResult buildAndFail() {
        return prepareAndBuild(true);
    }
}
