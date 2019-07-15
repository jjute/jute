package io.yooksi.jute.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public interface ProjectPlugin extends Plugin<Project> {

    /**
     * @return ID used to resolve this plugin.
     */
    String getId();
}
