package io.jjute.plugin.testsuite;

import org.gradle.api.Project;

import javax.validation.constraints.NotEmpty;

/**
 * Gradle properties are a mechanism used for configuring behavior of Gradle itself and specific projects.
 * To create a new property use the {@link Type#create(String, String) Type.create(String, String)}
 * method of the appropriate type. Read more information in the official Gradle documentation.
 *
 * @see <a href="https://docs.gradle.org/current/userguide/build_environment.html">
 *      Gradle docs: Build Environment</a>
 */
public class GradleProperty {

    public enum Type {

        /**
         * <p>
         *     This type represents a system property passed to the JVM which runs Gradle.
         * <p>
         *     The {@code -D} option of the gradle command has the same effect as the
         *     {@code -D} java command option. System properties can also be set in
         *     {@code gradle.properties} files with the prefix {@code systemProp}.
         *
         * @see <a href="https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_system_properties">
         *      Gradle Docs: Build Environment (System Properties)</a>
         */
        SYSTEM("-D"),

        /**
         * <p>
         *     This type represents properties that are defined for the current project build.
         * <p>
         *     Gradle can also set project properties when it sees specially-named system
         *     properties or environment variables. If the environment variable name looks
         *     like {@code ORG_GRADLE_PROJECT_prop=somevalue}, then Gradle will set a prop
         *     property on your project object, with the value of {@code somevalue}.
         * <p>
         *     Note that if a project property is referenced but does not exist, an exception will
         *     be thrown and the build will fail. You should check for existence of optional project
         *     properties with {@link Project#hasProperty(String)} before you try to access them.
         *
         * @see <a href="https://docs.gradle.org/current/userguide/build_environment.html#sec:project_properties">
         *      Gradle Docs: Build Environment (Project Properties)</a>
         */
        PROJECT("-P");

        private final String option;
        Type(String opt) { this.option = opt; }

        /**
         * Create and return a new Gradle property of this type with the given name and value.
         */
        public GradleProperty create(@NotEmpty String name, String value) {
            return new GradleProperty(this, name, value);
        }
    }

    private final javafx.util.Pair<String, String> data;
    private final GradleProperty.Type type;

    private GradleProperty(Type type, @NotEmpty String key, String value) {

        this.data = new javafx.util.Pair<>(key, value);
        this.type = type;
    }

    /**
     * @return the left-hand side of this property.
     */
    public @NotEmpty String getName() {
        return data.getKey();
    }

    /**
     * @return the right-hand side of this property.
     */
    public String getValue() {
        return data.getValue();
    }

    /**
     * @return a view of how this property as if it was passed as a command line option.
     *         If you need to display as if defined in a file see {@link #asProperty()}.
     */
    public @NotEmpty String asArgument() {
        return type.option + toString();
    }

    /**
     * @return a view of how this property as if it was defined in {@code gradle.properties}.
     *         If you need to display as if it was passed as a CL option see {@link #asArgument()}.
     */
    public @NotEmpty String asProperty() {
        return (type == Type.SYSTEM ? "systemProp." : "") + toString();
    }

    @Override
    public String toString() {
        return data.getKey() + '=' + data.getValue();
    }
}
