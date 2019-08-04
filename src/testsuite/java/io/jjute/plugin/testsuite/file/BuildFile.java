package io.jjute.plugin.testsuite.file;

import io.jjute.plugin.framework.ProjectPlugin;
import io.jjute.plugin.framework.define.JuteDependency;
import io.jjute.plugin.testsuite.core.PluginTestException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.Set;

/**
 * Each {@code BuildFile} instance represents a Gradle build configuration script file located
 * in the root directory of the project being tested. This class allows you to construct a
 * build script with user-defined specifications intended for use in testing environments.
 * <p>
 *     Although build script files are conventionally named {@link org.gradle.api.Project#
 *     DEFAULT_BUILD_FILE build.gradle} custom names are supported although discouraged
 *     to maintain simplicity in testing environments.
 * <p>
 *     All public methods are expected to conform to a standard builder design pattern.
 *     This means that the file is not actually written to until builder output has been
 *     finalized. Use {@link #create(File)} method to create a new builder instance and
 *     {@link Writer#sign() Writer.sign()} method to finalize the {@code BuildFile}.
 * </p>
 * @see <a href="https://docs.gradle.org/current/userguide/tutorial_using_tasks.html">
 *      Gradle Docs: Build Script Basics</a>
 */
@TestOnly
public class BuildFile extends File {

    private final Set<ProjectPlugin> plugins;
    private final Set<JuteDependency> dependencies;

    private BuildFile(Path buildDir, String filename, Writer writer) {

        super(buildDir.toString(), filename);
        this.plugins = writer.plugins;
        this.dependencies = writer.dependencies;
    }

    /**
     * @param buildDir root directory for the build file to construct
     * @return a new {@code BuildFile.Writer} instance intended to construct a {@code BuildFile}
     *         representing a file named {@code build.gradle} that resides in the given root directory.
     */
    public static Writer create(@NotNull File buildDir) {
        return new Writer(buildDir.toPath(), org.gradle.api.Project.DEFAULT_BUILD_FILE);
    }

    /**
     * @param buildDir root directory for this build file
     * @param filename filename representing a Gradle build configuration script file. If the given filename
     *                 does not represent a Gradle script file by having a {@code .gradle} extension then
     *                 the named extension will be manually appended to the given filename.
     *
     * @return a new {@code BuildFile.Writer} instance intended to construct a {@code BuildFile} that
     *         represents a custom named Gradle script file that resides in the given root directory.
     */
    public static Writer create(@NotNull File buildDir, @NotEmpty String filename) {

        boolean isFilenameGradleScript = FilenameUtils.getExtension(filename).equals("gradle");
        return new Writer(buildDir.toPath(), isFilenameGradleScript ? filename : filename + ".gradle");
    }

    public static class Writer {

        private final Path buildDir;
        private final String filename;

        private Set<ProjectPlugin> plugins;
        private Set<JuteDependency> dependencies;

        private final TextEntry.Map entries = new TextEntry.Map();

        private Writer(Path buildDir, String filename) {

            this.buildDir = buildDir;
            this.filename = filename;
        }

        /**
         * Apply the given plugins with a Gradle DSL block that configures an instance
         * of {@link org.gradle.plugin.use.PluginDependenciesSpec PluginDependenciesSpec}
         * which is an API used by plugin declarations inside the {@code plugins{}} script block.
         * <p>
         *     Each plugin will be declared in a new line with one of these format depending on
         *     if it's a community or core plugin, the only difference being the quotation type:
         * <ul>
         *     {@code id "community.plugin.id" ~ id 'core.plugin.id'}
         * </ul>
         * @param plugins array of project plugins to apply
         * @return instance of this {@code BuildFile.Writer}
         *
         * @see <a href="https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block">
         *      Gradle Docs: Applying plugins with the plugins DSL</a>
         */
        public Writer applyPlugins(ProjectPlugin... plugins) {

            this.plugins = java.util.Collections.unmodifiableSet(java.util.
                    Arrays.stream(plugins).collect(java.util.stream.Collectors.toSet()));

            String[] declarations = new String[plugins.length];
            for (int i = 0; i < plugins.length; i++) {
                declarations[i] = plugins[i].toString();
            }
            return writeDSLBlock("plugins", declarations);
        }

        /**
         * Declare module dependencies with a Gradle DSL block that configures an instance
         * of {@link org.gradle.api.artifacts.dsl.DependencyHandler DependencyHandler}.
         *
         * Dependencies passed as parameter are considered external dependencies and are
         * expected to have a valid configuration and notation. Each dependency will be
         * declared in a new line with a string notation formatted in this way:
         * <ul>
         *     {@code configurationName "group:name:version:classifier@extension"}
         * </ul>
         * @param dependencies array of <b>external</b> dependencies to declare.
         * @return instance of this {@code BuildFile.Writer}
         *
         * @see <a href="https://docs.gradle.org/current/userguide/declaring_dependencies.html">
         *      Gradle Docs: Declaring a dependency to a module</a>
         */
        public Writer declareExternalDependencies(JuteDependency... dependencies) {

            this.dependencies = java.util.Collections.unmodifiableSet(java.util.
                    Arrays.stream(dependencies).collect(java.util.stream.Collectors.toSet()));

            String[] declarations = new String[dependencies.length];
            for (int i = 0; i < dependencies.length; i++) {
                JuteDependency dependency = dependencies[i];
                declarations[i] = dependency.getConfiguration() + " \"" + dependency.getIdentifier() + '\"';
            }
            return writeDSLBlock("dependencies", declarations);
        }

        /**
         * Store the given array as a {@link TextEntry} that will be written to build file
         * after {@link #sign()} method has been called. The text will be written to file
         * without further formatting while maintaining natural insertion order based on
         * method invocation time compared to other registered {@code TextEntry} objects.
         *
         * @param text array of text lines to write to file.
         *             {@code null} entries produce blank lines.
         *
         * @return instance of this {@code BuildFile.Writer}.
         * @throws IllegalArgumentException if the string array is empty.
         */
        public Writer write(String[] text) {

            entries.put(text);
            return this;
        }

        /**
         * Format the given {@code String} array as a DSL script block where the block
         * name will match {@code name} parameter followed by {@code argument} parameter.
         * The block will be stored as a {@link TextEntry} that will be written to build
         * file after {@link #sign()} method has been called. The text will be written
         * to file while maintaining natural insertion order based on method invocation
         * time compared to other registered {@code TextEntry} objects.
         *
         * @param name name of the DSL script block.
         * @param argument <i>(optional)</i> DSL script block argument.
         * @param lines contents of the DSL script block.
         * @return instance of this {@code BuildFile.Writer}
         *
         * @throws IllegalArgumentException if {@code name} parameter is an empty {@code String} or
         *                                  {@code lines} parameters is an empty array.
         * @see #write(String[])
         */
        public Writer writeDSLBlock(String name, @Nullable String argument, String[] lines) {

            String blockName = name + (argument != null ? ' ' + argument : "");
            String[] dslBlock = constructDSLBlock(blockName, lines);
            dslBlock[dslBlock.length - 1] = "}\n";

            entries.put(name, dslBlock);
            return this;
        }

        /**
         * Format the given {@code String} array as a DSL script block where the block name
         * will match {@code name} parameter. The block will be stored as a {@link TextEntry}
         * that will be written to build file after {@link #sign()} method has been called.
         * The text will be written to file while maintaining natural insertion order based
         * on method invocation time compared to other registered {@code TextEntry} objects.
         *
         * @param name name of the DSL script block.
         * @param lines contents of the DSL script block.
         * @return instance of this {@code BuildFile.Writer}
         *
         * @throws IllegalArgumentException if {@code name} parameter is an empty {@code String} or
         *                                  {@code lines} parameters is an empty array.
         * @see #writeDSLBlock(String, String, String[])
         */
        public Writer writeDSLBlock(String name, String[] lines) {
            return writeDSLBlock(name, null, lines);
        }

        /**
         * Format the given {@code String} array as a DSL script block where the block
         * name will match {@code name} parameter. Each line will be indented with
         * {@code \t} to improve readability when debugging or storing build logs.
         *
         * @param name name of the DSL script block.
         * @param lines contents of the DSL script block.
         * @return instance of this {@code BuildFile.Writer}
         */
        private static String[] constructDSLBlock(String name, String[] lines) {

            for (int i = 0; i < lines.length; i++) {
                lines[i] = '\t' + lines[i];
            }
            String[] dslBlock = new String[lines.length + 2];
            System.arraycopy(lines, 0, dslBlock, 1, lines.length);
            dslBlock[0] = name + " {"; dslBlock[dslBlock.length - 1] = "}";
            return dslBlock;
        }

        /**
         * Sign the current configuration finalizing builder product.
         * <p>
         *     Create a new {@code BuildFile} with the specified directory and filename and
         *     write the {@code String} lists represented by each stored {@link TextEntry}
         *     item to the build {@code File} line by line. The lines will overwrite
         *     existing content rather then being added to the end of the file.
         * </p>
         * @return the newly created {@code BuildFile} instance.
         * @throws PluginTestException if an I/O error occurred while creating or writing to build file.
         */
        public BuildFile sign() {

            BuildFile buildFile = new BuildFile(buildDir, filename, this);
            try {
                if (!buildFile.createNewFile()) {
                    throw new FileAlreadyExistsException("Project build file already exists.");
                }
                FileUtils.writeLines(buildFile, entries.getTextLines(), false);
                return buildFile;
            }
            catch (IOException e) {
                String message = "Failed to construct build script: \"" + buildFile.getPath() + '\"';
                throw new PluginTestException(message, e);
            }
        }
    }

    /**
     * @return an immutable {@code Set} of declared plugins for this {@code BuildFile}.
     */
    public Set<ProjectPlugin> getDeclaredPlugins() {
        return plugins;
    }
    /**
     * @return an immutable {@code Set} of declared dependencies for this {@code BuildFile}.
     */
    public Set<JuteDependency> getDeclaredDependencies() {
        return dependencies;
    }
}
