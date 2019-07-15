package io.yooksi.jute.plugin;

import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;

@SuppressWarnings("WeakerAccess")
public class PluginUtils {

    private final Project project;
    private final Logger logger;

    public PluginUtils(Project project) {
        this.project = project;
        this.logger = project.getLogger();
    }

    /**
     * Run a <i>platform-independent</i> shell command.
     */
    public void runShellCommand(String command) {

        logger.debug("Running shell command " + command);
        String[] cmdArgs = command.split(" ");

        java.io.OutputStream stdout = new ByteArrayOutputStream();
        String[] fullCmdArgs = new String[cmdArgs.length + 2];

        if (System.getProperty("os.name").startsWith("Windows")) {
            fullCmdArgs[0] = "cmd"; fullCmdArgs[1] = "/c";
        }
        else {
            fullCmdArgs[0] = "sh"; fullCmdArgs[1] = "-c";
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
     * @throws FileNotFoundException if the given file is not a valid directory.
     * @throws MalformedURLException if the parsed URL failed to comply with the
     *                               specific syntax of the associated protocol.
     * @throws IllegalStateException if the target directory does not exist and we were
     *                               unable to create the missing directory structure
     */
    public void downloadTextFile(String url, File dir, boolean create) throws IOException {

        String filePath = dir.getPath();
        logger.debug(String.format("Downloading file: %s\nDestination dir: %s", url, filePath));

        if (dir.exists()) {
            if (!dir.isDirectory())
                throw new FileNotFoundException("Target path is not a valid directory!");
        }
        else if (!dir.mkdirs()) {
            throw new IllegalStateException("Unable to create directory structure for path: " + filePath);
        }
        final URL location = new URL(url);
        File target = dir.toPath().resolve(location.getFile()).toFile();

        if (create && !target.createNewFile()) {
            throw new FileAlreadyExistsException("Unable to create target file " + target.getName());
        }
        String text = ResourceGroovyMethods.getText(location);
        ResourceGroovyMethods.write(target, text);
    }
}
