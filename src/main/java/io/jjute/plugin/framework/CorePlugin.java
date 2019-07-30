package io.jjute.plugin.framework;

import org.gradle.api.Project;

/**
 * Core plugins are plugins which Gradle provides as part of its distribution.
 * <p>They are automatically resolved and do not need to be fully qualified.
 *
 * @see <a href="https://docs.gradle.org/current/userguide/plugin_reference.html">
 *      Gradle ProjectPlugin Reference (list of all core plugins)</a>
 */
public enum CorePlugin implements ProjectPlugin {
    /**
     * <p>
     *     The IDEA plugin generates files that are used by IntelliJ IDEA, thus making
     *     it possible to open the project from IDEA {@code (File - Open Project)}.
     *     Both external dependencies (including associated source and Javadoc files)
     *     and project dependencies are considered.
     * <p>
     *     What exactly the IDEA plugin generates depends on which other plugins are used:
     * <blockquote>
     * <dl>
     *     <dt><b>Always</b></dt>
     *     <dd>
     *         Generates an IDEA module file. Also generates an IDEA project
     *         and workspace file if the project is the root project.
     *     </dd>
     *     <dt>{@link #JAVA Java ProjectPlugin}</dt>
     *     <dd>
     *         Additionally adds Java configuration to the IDEA module and project files.
     *     </dd>
     * </dl>
     * </blockquote>
     * <p>
     *     One focus of the IDEA plugin is to be open to customization. The plugin provides a
     *     standardized set of hooks for adding and removing content from the generated files.
     * </p>
     * @see <a href="https://docs.gradle.org/current/userguide/idea_plugin.html">
     *      The IDEA ProjectPlugin Official Gradle Documentation</a>
     */
    IDEA("idea"),

    /**
     * The Java plugin adds Java compilation along with testing and bundling
     * capabilities to a project. It serves as the basis for many of the other
     * JVM language Gradle plugins. You can find a comprehensive introduction
     * and overview to the Java ProjectPlugin in the Building Java Projects chapter.
     *
     * @see <a href="https://docs.gradle.org/current/userguide/java_plugin.html#java_plugin">
     *      The Java ProjectPlugin Official Gradle Documentation</a>
     */
    JAVA("java"),

    /**
     * The Java Library plugin expands the capabilities of the Java plugin by providing
     * specific knowledge about Java libraries. In particular, a Java library exposes an
     * API to consumers (i.e., other projects using the Java or the Java Library plugin).
     * All the source sets, tasks and configurations exposed by the Java plugin are
     * implicitly available when using this plugin.
     *
     * @see <a href="https://docs.gradle.org/current/userguide/java_library_plugin.html">
     *      The Java Library ProjectPlugin Official Gradle Documentation</a>
     */
    JAVA_LIBRARY("java-library");

    private final String id;
    CorePlugin(String id) { this.id = id; }

    @Override
    public void apply(Project target) {
        target.getPluginManager().apply(id);
    }
    @Override
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "id '" + getId() + "'";
    }
}
