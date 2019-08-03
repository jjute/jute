package io.jjute.plugin.framework.define;

import io.jjute.plugin.framework.util.DependencyUtils;
import org.gradle.api.Incubating;
import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@NonNullApi
public class JuteExternalDependency implements JuteDependency {

    public static final JuteExternalDependency DUMMY_DEPENDENCY =
            new JuteExternalDependency("group", "name", "1.0");

    private final String group;
    private final String name;
    private final String version;

    private final String identifier;
    private final String configuration;

    private @Nullable String reason;

    public JuteExternalDependency(String group, String name, String version, String configuration) {

        this.group = group; this.name = name; this.version = version;
        this.identifier = DependencyUtils.getDependencyNotation(group, name, version);
        this.configuration = configuration;
    }

    public JuteExternalDependency(String group, String name, String version) {
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
        return new JuteExternalDependency(group, name, version, configuration);
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
