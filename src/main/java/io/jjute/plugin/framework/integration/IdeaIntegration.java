package io.jjute.plugin.framework.integration;

import io.jjute.plugin.framework.CorePlugin;
import io.jjute.plugin.framework.JutePlugin;
import io.jjute.plugin.framework.PluginConfig;
import org.gradle.api.Project;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.plugins.ide.idea.IdeaPlugin;
import org.gradle.plugins.ide.idea.model.IdeaModule;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Paths;

public class IdeaIntegration extends IntegrationModel {

    private final JutePlugin jute;
    private final PluginConfig config;

    public IdeaIntegration(JutePlugin plugin, Project target) {
        super("idea", target);
        this.jute = plugin;
        this.config = jute.getConfig();
    }

    /**
     * Fine-tune module details (*.iml file) of the IDEA plugin.
     * <p>Typically you don't have to configure this model directly because Gradle configures it for you.
     *
     * @throws PluginIntegrationException if the {@code idea} plugin was not found or applied to project.
     * @see org.gradle.plugins.ide.idea.model.IdeaModel
     */
    public void configureIdeaModel() throws PluginIntegrationException {

        PluginContainer pluginContainer = project.getPlugins();

        if (!jute.getProjectPlugins().contains(CorePlugin.IDEA) || !pluginContainer.hasPlugin("idea")) {
            throw new PluginIntegrationException(this, "Plugin was not found or applied.");
        }
        IdeaPlugin idea = pluginContainer.getPlugin(IdeaPlugin.class);
        idea.getModel().module(this::configureIdeaModule);
    }

    /**
     * Configures IDEA module information.
     * @see org.gradle.plugins.ide.idea.model.IdeaModel
     */
    private void configureIdeaModule(IdeaModule module) {
        /*
         * If true, output directories for this module will be located below
         * the output directory for the project otherwise, they will be set to
         * the directories specified by getter method return values.
         */
        module.setInheritOutputDirs(jute.getConfig().ideaInheritOutputDirs);

        File outputDir = validateOutputDirectory(config.ideaOutputDir);
        File testOutputDir = validateOutputDirectory(config.ideaTestOutputDir);

        module.setOutputDir(outputDir);
        module.setTestOutputDir(testOutputDir);

        module.setTargetBytecodeVersion(config.getJavaVersion());
    }

    /**
     * Validate the given directory path to make sure it's either a <i>valid</i> directory
     * {@link java.nio.file.Path Path} or {@code null} which means the configuration option
     * for this IDEA output path has not been designated.
     *
     * @param dirPath {@code String} representation of the output directory path.
     *                Can be {@code null} but cannot be an invalid {@code Path}.
     *
     * @return the {@code File} representation of the given path.
     *
     * @throws PluginIntegrationException if the directory path already exists but is not a directory,
     *                                    or the given {@code String} does not represent a valid {@code Path}.
     */
    private File validateOutputDirectory(@Nullable String dirPath) {

        try {
            /* Retrieving an invalid path here will throw a InvalidPathException
             * which we are trying to handle in the catch block
             */
            File outputDir = dirPath != null ? Paths.get(dirPath).toFile() : null;
            /*
             * Validate that an existing directory path is a real directory
             */
            if (dirPath != null && outputDir.exists() && !outputDir.isDirectory()) {
                throw new PluginIntegrationException(getModelName(), project, new java.nio.file.
                        NotDirectoryException("IDEA output path exists but is not a directory: " + dirPath));
            }
            else return outputDir;
        }
        catch (java.nio.file.InvalidPathException e) {
            throw new PluginIntegrationException(getModelName(), project, e);
        }
    }
}
