package io.jjute.plugin.framework;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class PluginUtils {

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
