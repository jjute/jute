package io.jjute.plugin.framework;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

/**
 * The {@code GradleProperties} class represents a persistent set of Gradle properties.
 */
public class GradleProperties extends Properties {

    /**
     * Find and return properties file with the given name from registered
     * resource {@code SourceDirectorySet} entries for current module.
     *
     * @param filename properties filename <b>with</b> the file extension.
     * @return {@code GradleProperties} object representing found properties.
     *
     * @throws IOException if an error occurred when reading from the input stream.
     * @throws IllegalArgumentException if the given filename is not a valid properties filename
     *                                  or a malformed Unicode escape appears in the file.
     * @see Class#getResourceAsStream(String)
     */
    public static @Nullable GradleProperties getFromResources(String filename) throws IOException {

        if (!FilenameUtils.getExtension(filename).equals("properties")) {
            throw new IllegalArgumentException(String.format(
                    "Parameter (%s) is not a valid properties filename.", filename));
        }
        try (java.io.InputStream stream = GradleProperties.class.getResourceAsStream('/' + filename))
        {
            if (stream != null)
            {
                GradleProperties properties = new GradleProperties();
                properties.load(stream); return properties;
            }
            else return null;
        }
    }

    /**
     * Find and return {@code default.properties} file from registered
     * resource {@code SourceDirectorySet} entries for current module.
     *
     * @return {@code GradleProperties} object representing found properties.
     * @throws IOException if an error occurred when reading from the input stream.
     *
     * @see #getFromResources(String)
     */
    public static @Nullable GradleProperties getDefaultFromResources() throws IOException {
        return getFromResources("default.properties");
    }
    /**
     * Find and return {@code gradle.properties} file from registered
     * resource {@code SourceDirectorySet} entries for current module.
     *
     * @return {@code GradleProperties} object representing found properties.
     * @throws IOException if an error occurred when reading from the input stream.
     *
     * @see #getFromResources(String)
     */
    public static @Nullable GradleProperties getFromResources() throws IOException {
        return getFromResources("gradle.properties");
    }

    /**
     * Writes this property list <i>(key and element pairs)</i> in this {@code Properties}
     * table to the given file using {@code FileOutputStream}.
     *
     * The target file will be created if it does not already exist.
     *
     * After the entries have been written, the output stream is flushed.
     * The output stream is automatically closed after this method returns.
     *
     * @throws FileNotFoundException if the given file exists but is a directory rather
     *                               than a regular file, does not exist but cannot be
     *                               created, or cannot be opened for any other reason.
     *
     * @throws IOException if writing this property list to the specified
     *                     output stream throws an {@code IOException.}
     *
     * @see #store(OutputStream, String)
     */
    public void storeToFile(File file) throws IOException {

        try (java.io.FileOutputStream stream = new java.io.FileOutputStream(file)) {
            store(stream, "Gradle properties");
        }
    }
}
