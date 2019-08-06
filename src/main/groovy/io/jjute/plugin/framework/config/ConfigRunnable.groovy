package io.jjute.plugin.framework.config

import io.jjute.plugin.framework.PluginConfig
import org.gradle.api.Project

abstract class ConfigRunnable implements Runnable {

    protected final Project project
    protected final PluginConfig config

    protected ConfigRunnable(ProjectConfigurator configurator) {
        this.project = configurator.project
        this.config = configurator.config
    }
}
