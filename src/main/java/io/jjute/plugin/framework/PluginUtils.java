package io.jjute.plugin.framework;

import org.apache.commons.io.FilenameUtils;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class PluginUtils {

    private final Project project;
    private final Logger logger;

    public PluginUtils(Project project) {
        this.project = project;
        this.logger = project.getLogger();
    }

    /**
     * @return the value of the given property or {@code null} if not found.
     * @see Project#findProperty(String)
     */
    public String findProperty(String property) {
        return (String) project.findProperty(property);
    }

    /**
     * Run a <i>platform-independent</i> shell command.
     */
    public void runShellCommand(String command) {

        logger.debug("Running shell command " + command);
        String[] cmdArgs = command.split(" ");

        java.io.OutputStream stdout = new java.io.ByteArrayOutputStream();
        String[] fullCmdArgs = new String[cmdArgs.length + 2];

        if (System.getProperty("os.name").startsWith("Windows")) {
            fullCmdArgs[0] = "cmd";
            fullCmdArgs[1] = "/c";
        } else {
            fullCmdArgs[0] = "sh";
            fullCmdArgs[1] = "-c";
        }
        System.arraycopy(cmdArgs, 0, fullCmdArgs, 2, cmdArgs.length);

        project.exec((execSpec) -> execSpec.commandLine((Object[]) fullCmdArgs));
        logger.quiet(stdout.toString().trim());
    }

    /**
     * Execute a Git command with the given arguments.
     * The output will be printed to console
     */
    public void runGitCommand(String command) {
        runShellCommand("git " + command);
    }

    /**
     * Download a text-based file from a given URL and store it in the given directory.
     *
     * @param url location of the file on the web
     * @param dir directory file used to store the target file
     *
     * @throws NotDirectoryException  if the given file is not a valid directory.
     * @throws java.net.MalformedURLException if the given URL is not valid.
     */
    public void downloadTextFile(String url, File dir, boolean create) throws IOException {

        String filePath = dir.getPath();
        logger.debug(String.format("Downloading file: %s\nDestination dir: %s", url, filePath));

        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new NotDirectoryException("Target path is not a valid directory!");
            }
        } else if (!dir.mkdirs()) {
            throw new IllegalStateException("Unable to create directory structure for path: " + filePath);
        }
        final URL location = new URL(url);
        File target = dir.toPath().resolve(location.getFile()).toFile();

        if (create && !target.createNewFile()) {
            throw new java.nio.file.FileAlreadyExistsException("Unable to create target file " + target.getName());
        }
        String text = ResourceGroovyMethods.getText(location);
        ResourceGroovyMethods.write(target, text);
    }

    /**
     * Find and return the properties file with the given name as a resource.
     * Resources are searched in registered resource {@code SourceDirectorySet}
     * entries for this module using {@link Class#getResourceAsStream(String)}.
     *
     * @param name properties filename <b>without</b> the file extension.
     * @return {@code Properties} object that represents the found properties file.
     *
     * @throws IOException if an I/O exception occurred while closing input stream.
     * @throws java.io.FileNotFoundException if the properties file was not found as a resource.
     */
    public static Properties getResourcePropertiesFile(@NotNull String name) throws IOException {

        String filename = '/' + name + ".properties";
        Properties properties = new Properties();

        try (java.io.InputStream stream = PluginUtils.class.getResourceAsStream(filename)) {
            if (stream == null) {
                String log = "Unable to read resource properties file (%s)";
                throw new java.io.FileNotFoundException(String.format(log, name));
            }
            properties.load(stream);
            return properties;
        }
    }

    /**
     * Search the given classpath for a specified path entry.
     *
     * @param classpath system classpath as a list of files.
     * @param entry {@code Path} entry to search for.
     * @return {@code File} instance that points to the found path entry or {@code null}
     *         if the given entry was not found on the provided classpath.
     *
     * @throws IOException if an I/O error is thrown when accessing a classpath directory.
     * @throws NoSuchFileException if any entry in the given classpath does not point to an existing file.
     */
    public static @Nullable File findClasspathEntry(List<? extends File> classpath, Path entry) throws IOException {

        for (File classpathElement : classpath)
        {
            List<Path> classpathDirs = java.nio.file.Files.walk(classpathElement.toPath())
                    .filter(Files::isDirectory).collect(Collectors.toList());

            for (Path dir : classpathDirs) {
                if (doesOriginPathContain(dir, entry))
                    return dir.toFile();
            }
        } return null;
    }

    /**
     * Directly compare <i>non-abstract</i> path elements of two paths to determine
     * if the {@code origin} path contains the sequence of path elements in the given path.
     *
     * @param origin sequence of {@code Path} elements to search <b>in</b>.
     * @param path sequence of {@code Path} elements to search <b>for</b>.
     * @return the result of the comparison - {@code true} if the given path is found
     *         to be a part of {@code origin} path and {@code false} otherwise.
     *
     * @throws IllegalArgumentException
     * if the given path contains no elements, empty elements or the contained elements
     * are <a href="http://teaching.idallen.com/dat2330/04f/notes/links_and_inodes.html">
     * special name-inode maps</a> and thus cannot be translated into a standalone path.
     */
    public static boolean doesOriginPathContain(Path origin, Path path) {

        final int pathNameCount = path.getNameCount();
        final String sPath = FilenameUtils.normalize(path.toString());

        if (pathNameCount == 0 || sPath == null || sPath.isEmpty()) {
            throw new IllegalArgumentException("Cannot compare paths because second path has no elements.");
        }
        else if (pathNameCount > origin.getNameCount()) {
            return false;
        }
        for (int i1 = 0, i2 = 0; i1 < origin.getNameCount(); i1++, i2 = 0)
        {
            for (; i2 < pathNameCount; i2++) {
                if (i1 + i2 >= origin.getNameCount() || !origin.getName(i1 + i2).equals(path.getName(i2)))
                    break;
            }
            if (i2 == pathNameCount)
                return true;
        }
        return false;
    }
}
