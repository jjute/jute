package io.jjute.plugin.testsuite.file;

import io.jjute.plugin.testsuite.core.IntegrationTest;
import org.apache.commons.io.FileUtils;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;

class BuildFileBTest extends IntegrationTest {

    @Test
    void shouldAddMavenAndIvyRepositories() throws IOException {

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
}
