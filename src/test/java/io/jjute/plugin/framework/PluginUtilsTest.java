package io.jjute.plugin.framework;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public class PluginUtilsTest {

    @Test
    public void shouldFindAndReadResourcePropertiesFiles() throws IOException {

        GradleProperties defaultProps = GradleProperties.getDefaultFromResources();
        Assertions.assertNotEquals(0, Objects.requireNonNull(defaultProps).size());

        GradleProperties gradleProps = GradleProperties.getFromResources("gradle.properties");
        Assertions.assertEquals(0, Objects.requireNonNull(gradleProps).size());
        Assertions.assertNotNull(GradleProperties.getFromResources());
    }

    @Test
    public void whenDirContainsPathShouldReturnTrue() {

        Path origin = Paths.get("example/of/some/really/fancy/path");
        String[] paths = { "some/really/fancy", "example/of", "fancy/path" };

        for (String sPath : paths) {
            Assertions.assertTrue(PluginUtils.doesOriginPathContain(origin, Paths.get(sPath)));
        }
    }

    @Test
    public void whenDirDoesNotContainPathShouldReturnFalse() {

        Path origin = Paths.get("example/of/some/really/fancy/path");
        String[] paths = { "example/of/not/fancy/path", "really/not/fancy", "not/fancy/path" };

        for (String sPath : paths) {
            Assertions.assertFalse(PluginUtils.doesOriginPathContain(origin, Paths.get(sPath)));
        }
    }

    @Test
    public void whenComparingPathWithNoElementsShouldThrowException() {

        Path origin = Paths.get("example/of/a/valid/path");
        String[] paths = { "", "/", "../", "./", "../.././" };

        for (String path : paths) {
            Assertions.assertThrows(IllegalArgumentException.class, () ->
                    PluginUtils.doesOriginPathContain(origin, Paths.get(path)));
        }
    }

    @Test
    public void shouldFindPathFromListOfFiles() throws IOException {

        Path rootDir = Files.createTempDirectory("jute-test");
        FileUtils.forceDeleteOnExit(rootDir.toFile());
        File[] files = {
                rootDir.resolve("documents/pictures/nature").toFile(),
                rootDir.resolve("documents/GitHub/jjute/commons").toFile(),
                rootDir.resolve("temp/212gff312").toFile()
        };
        for (File file : files) {
            Assertions.assertTrue(file.mkdirs());
        }
        Path child = java.nio.file.Paths.get("/Github/jjute");
        File result = Objects.requireNonNull(PluginUtils.findClasspathEntry(java.util.Arrays.asList(files), child));
        Assertions.assertEquals(files[1].getPath(), result.getPath());
    }
}
