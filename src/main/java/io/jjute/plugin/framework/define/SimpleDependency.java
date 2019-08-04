package io.jjute.plugin.framework.define;

import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;

/**
 * Represents a basic {@code Dependency} model with only skeleton methods
 * providing just enough information to create and resolve simple dependencies.
 */
public interface SimpleDependency extends Dependency {

    /**
     * @return dependency identifier used to resolve this dependency in remote repositories.
     * @see org.gradle.api.artifacts.ArtifactIdentifier
     */
    String getIdentifier();

    /**
     * @return the requested target configuration of this dependency.
     * @see ModuleDependency#getTargetConfiguration()
     */
    String getConfiguration();

    /**
     * @return a {@code String} representation of this dependency's entry declaration inside
     *         {@code dependencies} DSL script block. This is useful when manually constructing
     *         the DSL script block by directly writing to build file in a testing scenario.
     *
     * @see org.gradle.api.artifacts.dsl.DependencyHandler
     */
    String toDSLDeclaration();
}
