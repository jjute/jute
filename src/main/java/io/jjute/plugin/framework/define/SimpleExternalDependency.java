package io.jjute.plugin.framework.define;

import io.jjute.plugin.framework.util.DependencyUtils;
import org.gradle.api.Incubating;
import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Represents a basic external {@code Dependency} model with only skeleton methods
 * providing just enough information to create and resolve simple dependencies.
 */
@NonNullApi
public class SimpleExternalDependency implements SimpleDependency {

    private final String group;
    private final String name;
    private final String version;

    /**
     * {@code String} notation that identifies this dependency.
     *
     * @see DependencyUtils#getDependencyNotation(Dependency)
     */
    private final String identifier;

    /**
     * {@code String} representation of {@code Configuration}
     * used to include this dependency in classpath.
     *
     * @see org.gradle.api.artifacts.Configuration
     */
    private final String configuration;

    private @Nullable String reason;

    public SimpleExternalDependency(String group, String name, String version, String configuration) {

        this.group = group; this.name = name; this.version = version;
        this.identifier = DependencyUtils.getDependencyNotation(group, name, version);
        this.configuration = configuration;
    }

    /**
     * Construct a new {@code SimpleExternalDependency} with {@link
     * JavaPlugin#IMPLEMENTATION_CONFIGURATION_NAME default} configuration.
     */
    public SimpleExternalDependency(String group, String name, String version) {
        this(group, name, version, JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME);
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getConfiguration() {
        return configuration;
    }

    @Override
    public @Nullable String getGroup() {
        return group;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public @Nullable String getVersion() {
        return version;
    }

    @Override
    public boolean contentEquals(@NotNull Dependency dependency) {
        return identifier.equals(DependencyUtils.getDependencyNotation(dependency));
    }

    @Override
    public Dependency copy() {
        return new SimpleExternalDependency(group, name, version, configuration);
    }

    @Override
    public @Nullable String getReason() {
        return reason;
    }

    @Override
    public @Incubating void because(@Nullable String reason) {
        this.reason = reason;
    }

    @Override
    public String toDSLDeclaration() {
        return configuration + " \"" + identifier + '\"';
    }
}
