package io.jjute.plugin.framework;

import groovy.lang.Closure;
import io.jjute.plugin.framework.parser.*;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;

import org.gradle.plugins.ide.idea.model.IdeaModule;

import java.io.File;
import java.lang.reflect.Field;

/**
 * <p>
 *     This is a Jute plugin DSL extension class.
 * <p>
 *     Fields declared in this class will be treated as project properties and as such should be
 *     declared {@code public} so they allow access in the following ways:
 * <ul>
 *     <li>Each field will be accessible directly from client {@code build.gradle}.
 *     <li>Field values will be updated from project properties on {@link Project#apply(Closure) Project.apply}.
 * </ul>
 * @see <a href="https://docs.gradle.org/4.10.3/userguide/custom_plugins.html#sec:getting_input_from_the_build">
 *      Gradle Docs 4.10.3: Making the plugin configurable</a>
 */
@SuppressWarnings({"unused", "WeakerAccess", "CanBeFinal"})
public class PluginConfig {

    /**
     * Java compatibility version used for compiling Java sources.
     *
     * @see JavaPluginConvention#setTargetCompatibility(JavaVersion)
     * @see JavaPluginConvention#setSourceCompatibility(JavaVersion)
     */
    public JavaVersion projectJavaVersion = JavaVersion.VERSION_1_8;

    /**
     * Whether this project should be considered a Java library or not.
     * When this is set to {@code true} the project will use {@code java-library}
     * core plugin otherwise the default {@code java} plugin will be used.
     *
     * @see CorePlugin#JAVA_LIBRARY
     * @see CorePlugin#JAVA
     */
    public boolean isProjectJavaLibrary = true;

    /**
     * Whether this project should integrate with Intellij IDEA via the {@code idea} core plugin.
     * @see CorePlugin#IDEA
     */
    public boolean enableIDEAIntegration = true;

    /**
     * The output directory for production classes used only by IDEA.
     * Gradle uses it's own build directory for storing outputs.
     *
     * @see IdeaModule#setOutputDir(File)
     */
    public String ideaOutputDir = "build/target/production";

    /**
     * The output directory for test classes used only by IDEA.
     * Gradle uses it's own build directory for storing outputs.
     *
     * @see IdeaModule#setTestOutputDir(File)
     */
    public String ideaTestOutputDir = "build/target/test";

    /**
     * If {@code true}, output directories for IDEA modules will be located
     * below the output directory for the project otherwise, they will be set
     * to the directories specified by getter method return values.
     *
     * @see IdeaModule#setInheritOutputDirs(Boolean)
     */
    public boolean ideaInheritOutputDirs = false;

    /**
     * Should tests perform using {@code JUnit} platform. If integration is disabled
     * and JUnit is the only project test suite available then tests will not run.
     */
    public boolean enableJUnitIntegration = true;

    /**
     * <p>
     *     Gradle plugin properties required by {@code JutePlugin}.
     * <p>
     *     Each property here comes with a parser reference used to parse corresponding
     *     properties found as {@code Object} instances found in Gradle properties.
     *     A {@code null} parser means the {@code Object} should be interpreted as a {@code String}.
     * </p>
     */
    public enum Property {

        JAVA_VERSION("projectJavaVersion", Property.Parser.javaVersionParser),
        IS_JAVA_LIBRARY("isProjectJavaLibrary", PrimitiveParser.BOOLEAN),
        IDEA_INTEGRATION("enableIDEAIntegration", PrimitiveParser.BOOLEAN),
        IDEA_OUTPUT_DIR("ideaOutputDir"),
        IDEA_TEST_OUTPUT_DIR("ideaTestOutputDir"),
        IDEA_INHERIT_DIRS("ideaInheritOutputDirs", PrimitiveParser.BOOLEAN),
        JUNIT_INTEGRATION("enableJUnitIntegration", PrimitiveParser.BOOLEAN),

        private final String name;
        private final Field field;
        private final DataParser parser;

        Property(String name, DataParser parser) {

            this.name = name;
            this.parser = parser;

            try {
                this.field = PluginConfig.class.getDeclaredField(name);
            }
            catch (NoSuchFieldException e) {
                throw new IllegalArgumentException(String.format("The specified field " +
                        "\"%s\" was not found declared in class PluginConfig", name), e);
            }
        }
        Property(String name) {
            this(name, null);
        }

        /**
         * Read this property from the given {@code Project} for the specified {@code JutePlugin}
         * instance using a {@code DataParser} to parse the found property {@code Object}.
         *
         * @param plugin {@code PluginConfig} owner loading this property.
         * @param project {@code Project} instance to search for property data.
         */
        @SuppressWarnings("unchecked")
        void loadFromProjectProperties(JutePlugin plugin, Project project) {

            Object property = project.findProperty(name);
            if (property instanceof String) {
                /*
                 * null parser indicated that the property should be interpreted as a String
                 */
                Object result = parser != null ? parser.parse(property) : property.toString();
                try {
                    field.set(plugin.getConfig(), result);
                }
                catch (IllegalAccessException e) {
                    throw new IllegalStateException(String.format("Unable to set field %s," +
                            " access through reflection was denied.", field.getName()), e);
                }
            }
        }
        /**
         * @return the name of this property used to access it through DSL.
         */
        public String getName() {
            return name;
        }

        private static class Parser {
            private static final DataParser javaVersionParser =
                    new ObjectParser<>(JavaVersion.class, "toVersion", Object.class);
        }
    }

    public JavaVersion getJavaVersion() {
        return projectJavaVersion;
    }
    public boolean isJavaLibrary() {
        return isProjectJavaLibrary;
    }
    public boolean JUnitIntegration() {
        return enableJUnitIntegration;
    }
    public boolean ideaIntegration() {
        return enableIDEAIntegration;
    }
    public String getIdeaOutputDir() {
        return ideaOutputDir;
    }
    public String getIdeaTestOutputDir() {
        return ideaTestOutputDir;
    }
}
