package io.jjute.plugin.testsuite.file;

import io.jjute.plugin.framework.define.CommonRepository;
import io.jjute.plugin.testsuite.core.ProjectTest;
import org.apache.commons.io.FileUtils;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;

class BuildFileBTest extends ProjectTest {

    private java.io.File build = buildDir.toPath().resolve("build.gradle").toFile();

    @BeforeEach
    void deleteExistingBuildFile() {
        Assertions.assertTrue(!build.exists() || build.delete());
    }

    @Test
    void shouldAddArtifactRepositories() throws IOException {

        RepositoryHandler repositoryHandler = project.getRepositories();
        MavenArtifactRepository[] repositoriesArray = {
                repositoryHandler.jcenter(),
                repositoryHandler.mavenCentral(),
                repositoryHandler.google()
        };
        String[] expectedDSLBlock = {
                "repositories {",
                "\tmaven {",
                "\t\turl \"" + repositoriesArray[0].getUrl().toString() + '\"', "\t}",
                "\tmaven {",
                "\t\turl \"" + repositoriesArray[1].getUrl().toString() + '\"', "\t}",
                "\tmaven {",
                "\t\turl \"" + repositoriesArray[2].getUrl().toString() + '\"', "\t}",
                "}", ""
        };
        java.util.Set<ArtifactRepository> repositories = java.util.Arrays.stream(
                repositoriesArray).collect(java.util.stream.Collectors.toSet());

        BuildFile result = BuildFile.create(buildDir).addRepositories(repositoriesArray).sign();
        Assertions.assertFalse(repositories.retainAll(result.getDeclaredRepositories()));

        java.util.List<String> expected = java.util.Arrays.asList(expectedDSLBlock);
        Assertions.assertEquals(expected, FileUtils.readLines(result, Charset.defaultCharset()));
    }

    @Test
    void shouldAddCommonRepositories() throws IOException {

        CommonRepository[] repositoriesArray = CommonRepository.values();
        String[] expectedDSLBlock = {
                "repositories {",
                '\t' + repositoriesArray[0].getName() + " {",
                "\t\turl \"" + repositoriesArray[0].getUrl().toString() + '\"', "\t}",
                '\t' + repositoriesArray[1].getName() + " {",
                "\t\turl \"" + repositoriesArray[1].getUrl().toString() + '\"', "\t}",
                '\t' + repositoriesArray[2].getName() + " {",
                "\t\turl \"" + repositoriesArray[2].getUrl().toString() + '\"', "\t}",
                "}", ""
        };
        java.util.Set<ArtifactRepository> repositories = java.util.Arrays.stream(
                repositoriesArray).collect(java.util.stream.Collectors.toSet());

        BuildFile result = BuildFile.create(buildDir).addRepositories(repositoriesArray).sign();
        Assertions.assertFalse(repositories.retainAll(result.getDeclaredRepositories()));

        java.util.List<String> expected = java.util.Arrays.asList(expectedDSLBlock);
        Assertions.assertEquals(expected, FileUtils.readLines(result, Charset.defaultCharset()));
    }
}
