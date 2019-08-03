package io.jjute.plugin.framework;

import io.jjute.plugin.framework.config.TestConfigurator;
import io.jjute.plugin.framework.integration.*;
import io.jjute.plugin.framework.parser.DataParsingException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.dsl.ScriptHandler;
import org.gradle.api.logging.Logger;

import java.util.Set;

public class JutePlugin implements Plugin<Project> {

    private PluginConfig config;
    private Set<ProjectPlugin> plugins;

    /**
     * <p>
     *     When the plugin is applied to a project, Gradle creates an instance of the plugin
     *     class and calls this method against the instance. The project object is passed as
     *     a parameter, which the plugin can use to configure the project however it needs to.
     * <p>
     *     A new instance of a plugin is created for each project it is applied to. Also note that
     *     the {@code Plugin} class is a generic type. This example has it receiving the {@code Project}
     *     type as a type parameter. A plugin can instead receive a parameter of type {@code Settings},
     *     in which case the plugin can be  applied in a settings script, or a parameter of type Gradle,
     *     in which case the plugin can be applied in an initialization script.
     * </p>
     * @param target {@code Project} object this plugin instance is being applied to
     * @see <a href="https://docs.gradle.org/4.10.3/userguide/custom_plugins.html#sec:writing_a_simple_plugin">
     *      Gradle Docs 4.10.3: Writing a simple plugin</a>
     */
    @Override
    public void apply(Project target) {

        Logger logger = target.getLogger();
        logger.debug("Applying JutePlugin to project " + target.getDisplayName());

        config = JutePlugin.createPluginConfig(target);

        java.util.Set<ProjectPlugin> pluginsList = new java.util.HashSet<>();
        pluginsList.add(config.isJavaLibrary() ? CorePlugin.JAVA_LIBRARY : CorePlugin.JAVA);
        if (config.ideaIntegration()) pluginsList.add(CorePlugin.IDEA);

        plugins = java.util.Collections.unmodifiableSet(pluginsList);

        ScriptHandler buildscript = target.getBuildscript();
        buildscript.getRepositories().gradlePluginPortal();

        target.allprojects( project -> {

            for (ProjectPlugin plugin : plugins) {
                plugin.apply(project);
            }
            /* Adds a repository which looks in Bintray's JCenter repository for dependencies.
             * The URL used to access this repository is "https://jcenter.bintray.com/".
             */
            project.getRepositories().jcenter();

            JavaIntegration java = new JavaIntegration(target);
            java.setCompileCompatibility(config.getJavaVersion());
            java.standardizeDirectoryLayout();

            if (plugins.contains(CorePlugin.IDEA)) {
                new IdeaIntegration(this, target).configureIdeaModel();
            }
            /*
             * Sets the build directory of this project.
             * The build directory is the directory which all artifacts are generated into.
             */
            project.setBuildDir(project.getProjectDir().toPath().resolve("build").toFile());


            if (config.JUnitIntegration()) {
                new JUnitIntegration(project).addProjectDependencies().enableNativeSupport();
            }
        });
    }

    /**
     * Create a new {@code PluginConfig} DSL extension type and update the available
     * fields with corresponding Gradle properties defined in {@code gradle.properties} file
     *
     * @param project {@code Project} to create the extension for
     * @return the created DSL extension
     */
    public static PluginConfig createPluginConfig(Project project) {

        PluginConfig createdConfig = project.getExtensions().create("jute", PluginConfig.class);
        JutePlugin.loadGradleProperties(project, createdConfig);
        return createdConfig;
    }

    /**
     * Load key/value pairs from {@code gradle.properties} file located inside the given
     * project's root directory and update corresponding {@code PluginConfig} fields.
     *
     * @throws PluginExecutionException if a {@link DataParsingException} occurred while loading
     *                                  properties from project properties
     */
    protected static void loadGradleProperties(Project project, PluginConfig config) {

        try {
            for (PluginConfig.Property value : PluginConfig.Property.values()) {
                value.loadFromProjectProperties(config, project);
            }
        } catch (DataParsingException e) {
            throw new PluginExecutionException("A fatal exception occurred while loading" +
                    " Gradle properties for project " + project.getName(), e);
        }
    }

    public Set<ProjectPlugin> getProjectPlugins() {
        return plugins;
    }

    public PluginConfig getConfig() {
        return config;
    }
}
