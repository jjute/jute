package io.jjute.plugin.framework;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public interface ProjectPlugin extends Plugin<Project> {

    /**
     * @return ID used to resolve this plugin.
     */
    String getId();

    /**
     * @return a {@code String} representation of this plugin's entry definition inside a
     *         {@code plugins} DSL script block. This is useful when manually constructing
     *         the DSL script block by directly writing to build file.
     */
    String toString();
}
