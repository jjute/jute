package io.jjute.plugin.testsuite.core;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Files;

public abstract class PluginTest {

    public enum Type {
        /**
         * Used to test the system from the end user’s perspective.
         * @see BuildTest
         */
        FUNCTIONAL("func"),

        /**
         * Verifies that multiple classes or components work together as a whole.
         * @see ProjectTest
         */
        INTEGRATION("int"),

        /**
         * Verifies the smallest unit of code which in Java translates to methods.
         * @see UnitTest
         */
        UNIT("unit");

        private final String prefix;
        Type(String prefix) {
            this.prefix = prefix;
        }
    }

    /**
     * The directory that Gradle will be executed in
     * which is also the root project of the build under test.
     */
    protected final File buildDir;

    /**
     * Prepare build test area in a new directory located in the system designated default temporary
     * file directory. The test area will be cleaned after the JVM that invoked the tests terminates.
     *
     * @throws PluginTestException if an {@code IOException} occurred while trying to create
     *                                   or schedule deletion of the root directory or build file.
     */
    PluginTest(Type type) {

        String dirName = "jute-" + type.prefix + "-test";
        try {
            buildDir = Files.createTempDirectory(dirName).toFile();
        }
        catch (java.io.IOException e) {
            throw new PluginTestException("Unable to " +
                    "create build root directory: \"" + dirName + "\"", e);
        }
        /*
         * Use a shutdown hook here because for some reason Apache
         * FileUtils.forceDeleteOnExit does not work in this specific situation.
         */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { FileUtils.deleteDirectory(buildDir); }
            catch (java.io.IOException e) {
                throw new PluginTestException("Failed to schedule " +
                        "build root dir deletion: \"%s\"", buildDir.getPath(), e);
            }
        }));
    }
}
