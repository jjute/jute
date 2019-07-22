package io.jjute.plugin.framework;

import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.dsl.ScriptHandler;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.Convention;
import org.gradle.api.plugins.JavaPluginConvention;

import java.util.Collections;
import java.util.Set;

public class JutePlugin implements Plugin<Project> {

    private Logger logger;
    private PluginUtils utils;
    private JavaVersion javaVersion;

    private PluginConfig config;
    private Set<ProjectPlugin> plugins;

    @Override
    public void apply(Project target) {

        logger = target.getLogger();
        utils = new PluginUtils(target);

        logger.debug("Applying JutePlugin to project " + target.getDisplayName());

        loadGradleProperties();
        config = target.getExtensions().create("jute", PluginConfig.class);

        ScriptHandler buildscript = target.getBuildscript();
        buildscript.getRepositories().gradlePluginPortal();

        target.allprojects( project -> {

            for (ProjectPlugin plugin : plugins) {
                plugin.apply(project);
            }
            /*
             * Adds a repository which looks in Bintray's JCenter repository for dependencies.
             * The URL used to access this repository is "https://jcenter.bintray.com/".
             */
            project.getRepositories().jcenter();

            Convention convention = project.getConvention();
            JavaPluginConvention javaConvention = convention.getPlugin(JavaPluginConvention.class);
            /*
             * Set source and target compatibility for compiling Java sources.
             * This will also define the Language Level that corresponds to the given Java version.
             */
            javaConvention.setSourceCompatibility(javaVersion);
            javaConvention.setTargetCompatibility(javaVersion);

        });
    }

    /**
     * Load key/value pairs from {@code gradle.properties} file in project
     * root directory and configure internal plugin properties.
     */
    private void loadGradleProperties() {

        logger.debug("Loading internal project properties.");

        String versionProp = utils.findProperty("projectJavaVersion");
        javaVersion = versionProp != null ? JavaVersion.toVersion(versionProp) : JavaVersion.VERSION_1_8;

        String isLibrary = utils.findProperty("isProjectJavaLibrary");
        String ideaIntegration = utils.findProperty("enableIDEAIntegration");

        java.util.Set<ProjectPlugin> pluginsList = new java.util.HashSet<>();

        pluginsList.add(Boolean.parseBoolean(isLibrary) ? CorePlugin.JAVA_LIBRARY : CorePlugin.JAVA);
        if (Boolean.parseBoolean(ideaIntegration)) {
            pluginsList.add(CorePlugin.IDEA);
        }
        plugins = Collections.unmodifiableSet(pluginsList);
    }

    public ProjectPlugin[] getProjectPlugins() {
        return plugins.toArray(new ProjectPlugin[0]);
    }

    public Logger getLogger() {
        return logger;
    }
}
