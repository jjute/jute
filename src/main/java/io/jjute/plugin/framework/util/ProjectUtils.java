package io.jjute.plugin.framework.util;

import org.gradle.api.Project;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.tasks.testing.Test;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProjectUtils {

    /**
     * <p>
     *     Find and collect all dependencies found in the given {@code Project}.
     *     The dependencies are searched in every project {@code Configuration} by retrieving the
     *     complete set of declared dependencies including those contributed by super-configurations.
     * <p>
     *     Note that this method does not resolve configurations, so the collected dependencies
     *     will not include transitive dependencies. Also, unknown or <i>unspecified</i>
     *     dependencies will not be included in the return value.
     * </p>
     * @param project the {@code Project} to search and collect dependencies from.
     * @return complete set of declared dependencies for the given project.
     */
    public static java.util.Set<Dependency> getProjectDependencies(Project project) {

        java.util.Set<Dependency> dependencies = new java.util.HashSet<>();
        for (Configuration configuration : project.getConfigurations()) {
            dependencies.addAll(configuration.getAllDependencies().stream()
                    .filter(d -> !d.getName().equals("unspecified"))
                    .collect(java.util.stream.Collectors.toSet()));
        }
        return dependencies;
    }

    /**
     * Search the given {@code Project} for the dependency with the specified notation.
     *
     * @param project the {@code Project} to search the dependency in.
     * @param notation {@code String} representation of the dependency to search.
     * @return {@code true} if a dependency that matches the specified notation exists for the
     *         given project and {@code false} otherwise.
     *
     * @see #getProjectDependencies(Project)
     */
    public static boolean projectHasDependency(Project project, String notation) {

        java.util.Set<Dependency> dependencies = getProjectDependencies(project);
        for (Dependency dependency : dependencies) {
            if (notation.equals(getDependencyNotation(dependency))) {
                return true;
            }
        } return false;
    }

    /**
     * Search the given {@code Project} for the dependency with the specified properties.
     *
     * @param project the {@code Project} to search the dependency in.
     * @param group the {@link Dependency#getGroup() group} of the dependency to find.
     * @param name the {@link Dependency#getName() name} of the dependency to find.
     * @param version the {@link Dependency#getVersion() version} of the dependency to find.
     *
     * @return {@code true} if the given project has the dependency with the specified
     *         properties and {@code false} otherwise.
     *
     * @see #projectHasDependency(Project, String)
     */
    public static boolean projectHasDependency(Project project, @Nullable String group, String name, @Nullable String version) {
        return projectHasDependency(project, getDependencyNotation(group, name, version));
    }

    /**
     * Construct and return a {@code String} representing a given dependency notation.
     * <p>
     *     Take the following dependency declaration as an example which this method would translate a
     *     {@code Dependency} reference with those properties to the following {@code String} format:
     * <pre>
     * compile group: 'org.apache.commons', name: 'commons-math3', version: '3.6.1'
     * dependency-notation: org.apache.commons:commons-math3:3.6.1
     * </pre>
     * <p>
     *     This format is intended to make it easier to compare dependency references, where before
     *     we would have to manually compare {@code group}, {@code name} and {@code version} now we
     *     can just compare the content of resolved notations as two {@code String} objects.
     */
    public static String getDependencyNotation(@NotNull Dependency dependency) {
        return dependency.getGroup() + ':' + dependency.getName() + ':' + dependency.getVersion();
    }

    /**
     * Construct and return a {@code String} representing a given dependency notation.
     * <p>
     *     Take the following dependency declaration as an example which this method would translate a
     *     {@code Dependency} reference with those properties to the following {@code String} format:
     * <pre>
     * compile group: 'org.apache.commons', name: 'commons-math3', version: '3.6.1'
     * dependency-notation: org.apache.commons:commons-math3:3.6.1
     * </pre>
     * <p>
     *     This format is intended to make it easier to compare dependency references, where before
     *     we would have to manually compare {@code group}, {@code name} and {@code version} now we
     *     can just compare the content of resolved notations as two {@code String} objects.
     * </p>
     * @param group the {@link Dependency#getGroup() group} of the dependency notation to construct.
     *              As with the actual dependency property this parameter value is {@code null} permissible.
     * @param name the {@link Dependency#getName() name} of the dependency notation to construct
     * @param version the {@link Dependency#getVersion() version} of the dependency notation to construct.
     *                As with the actual dependency property this parameter value is {@code null} permissible.
     *
     * @see #getDependencyNotation(Dependency)
     */
    @SuppressWarnings("UnnecessaryCallToStringValueOf")
    public static String getDependencyNotation(@Nullable String group, String name, @Nullable String version) {
        return String.valueOf(group) + ':' + name + ':' + String.valueOf(version);
    }

    /**
     * @return {@link Test} task in charge of executing JUnit (3.8.x, 4.x or 5.x) or TestNG tests.
     *         Note that this task if only available if either {@code java} or {@code java-library}
     *         plugin has been applied to the given project.
     *
     * @throws UnknownTaskException if no {@code Test} task with the name {@code test} has been found for the
     *                              given project. The primary cause for this is the absence and application
     *                              of {@code java} and {@code java-library} plugin to the given project.
     */
    public static Test getTestTask(Project project) {
        return project.getTasks().withType(Test.class).getByName("test");
    }
}
