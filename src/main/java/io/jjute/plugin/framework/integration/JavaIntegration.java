package io.jjute.plugin.framework.integration;

import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

public class JavaIntegration extends IntegrationModel {

    public JavaIntegration(Project target) {
        super("Java", target);
    }

    /**
     * Set source and target compatibility for compiling Java sources.
     * This will also define the language level that corresponds to the given {@code JavaVersion}.
     */
    public void setCompileCompatibility(JavaVersion version) {

        JavaPluginConvention convention = getConvention();

        convention.setSourceCompatibility(version);
        convention.setTargetCompatibility(version);
    }

    /**
     * This establishes a source directory convention for all sub-project. Having a common directory
     * layout allows for users familiar with one Maven project to immediately feel at home in
     * another Maven project. The advantages are analogous to adopting a site-wide look-and-feel.
     *
     * @see <a href="https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html">
     *      Apache Maven Project: Introduction to the Standard Directory Layout</a>
     */
    public void standardizeDirectoryLayout() {

        SourceSetContainer sourceSets = getConvention().getSourceSets();

        SourceSet main = sourceSets.getByName("main");
        SourceSet test = sourceSets.getByName("test");

        setSingleSourceDir(main.getJava(), "src/main/java");
        setSingleSourceDir(main.getResources(), "src/main/resources");

        setSingleSourceDir(test.getJava(), "src/test/java");
        setSingleSourceDir(test.getResources(), "src/test/resources");
    }

    /**
     * Sets a <b>single</b> source directory for the given {@code SourceDirectorySet}.
     * The {@code Path} resolved from the given {@code String} will override other
     * existing source directory entries and establish itself as sole entry.
     *
     * @param set {@code SourceDirectorySet} to set source directory for.
     * @param path {@code String} representation of the path to the source directory to set.
     */
    public void setSingleSourceDir(SourceDirectorySet set, String path) {

        java.nio.file.Path sourceDir = java.nio.file.Paths.get(path);
        set.setSrcDirs(java.util.Collections.singleton(sourceDir));
    }

    /**
     * @return the {@code JavaPluginConvention} for the integration target project.
     */
    public JavaPluginConvention getConvention() {
        return project.getConvention().getPlugin(JavaPluginConvention.class);
    }
}
