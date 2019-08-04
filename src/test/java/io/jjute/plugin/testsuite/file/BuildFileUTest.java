package io.jjute.plugin.testsuite.file;

import io.jjute.plugin.framework.CommunityPlugin;
import io.jjute.plugin.framework.CorePlugin;
import io.jjute.plugin.framework.ProjectPlugin;
import io.jjute.plugin.framework.define.SimpleDependency;
import io.jjute.plugin.framework.define.SimpleExternalDependency;
import io.jjute.plugin.framework.integration.JUnitIntegration;
import io.jjute.plugin.testsuite.core.UnitTest;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

class BuildFileUTest extends UnitTest {

    private static final SimpleExternalDependency DUMMY_DEPENDENCY =
            new SimpleExternalDependency("group", "name", "1.0");

    private static final Charset CHARSET = Charset.defaultCharset();

    private BuildFile.Writer buildWriter;

    @BeforeEach
    void createNewBuildFileInstance() {
        buildWriter = BuildFile.create(buildDir);
    }

    @Test
    void shouldCreateWriterWithCustomFilename() {
        validateWriterWithCustomFilename("custom", false);
    }

    @Test
    void shouldCreateWriterWithCustomGradleScriptFilename() {
        validateWriterWithCustomFilename("custom.gradle", true);
    }

    @TestOnly
    private void validateWriterWithCustomFilename(String filename, boolean gradleScript) {

        BuildFile buildFile = BuildFile.create(buildDir, filename).sign();
        String expected = gradleScript ? filename : filename + ".gradle";

        Assertions.assertEquals(expected, buildFile.getName());
        Assertions.assertEquals(buildDir.toPath().resolve(expected).toString(), buildFile.getPath());
    }

    @Test
    void shouldWriteToBuildFile() throws IOException {

        String[] text = { "first line", "second line", "third line" };
        BuildFile buildFile = buildWriter.write(text).sign();

        List<String> expected = Arrays.asList(text);
        Assertions.assertEquals(expected, FileUtils.readLines(buildFile, CHARSET));
    }

    @Test
    void shouldConstructAndWriteDSLBlock() throws IOException {

        String[] content = { "first statement", "second statement", };
        String[] expectedDSLBlock = constructDSLDeclarationBlock("sampleBlock", content);

        BuildFile result = buildWriter.writeDSLBlock("sampleBlock", content).sign();

        List<String> expected = Arrays.asList(expectedDSLBlock);
        Assertions.assertEquals(expected, FileUtils.readLines(result, CHARSET));

        // Delete the build file before creating a new writer
        Assertions.assertTrue(result.delete());

        String line = "// do something here...";
        expectedDSLBlock = new String[]{ "task awesomeTask {", '\t' + line, "}", "" };

        BuildFile.Writer newBuildWriter = BuildFile.create(buildDir);
        newBuildWriter.writeDSLBlock("task", "awesomeTask", new String[]{line});

        expected = Arrays.asList(expectedDSLBlock);
        Assertions.assertEquals(expected, FileUtils.readLines(newBuildWriter.sign(), CHARSET));
    }

    @Test
    void shouldApplyPluginsInDSLBlock() throws IOException {

        String[] expectedDSLBlock = {
                "id 'java'", "id 'idea'", "id \"com-pluginA\"",
                "id \"com-pluginB\" version \"1.0\""
        };
        ProjectPlugin[] pluginsArray = {
                CorePlugin.JAVA, CorePlugin.IDEA,
                new CommunityPlugin("com-pluginA"),
                new CommunityPlugin("com-pluginB", "1.0")
        };
        Set<ProjectPlugin> pluginsSet = new java.util.HashSet<>();
        java.util.Collections.addAll(pluginsSet, pluginsArray);

        BuildFile result = buildWriter.applyPlugins(pluginsArray).sign();

        assertEqualWriterSets(pluginsSet, result.getDeclaredPlugins());
        validateBuildFileWriterDSLBlock(result, "plugins", expectedDSLBlock);
    }

    @Test
    void shouldDeclareExternalDependenciesInDSLBlock() throws IOException {

        String[] dependenciesArray = {
                "net.group:first-sample-name:2.0",
                "com.group:second-sample-name:0.1",
                "org.group:third-sample-name:1.3"
        };
        final int length = dependenciesArray.length;
        String[] expectedDSLBlock = new String[length];

        SimpleDependency[] dependencies = new SimpleDependency[length];
        Set<SimpleDependency> dependencySet = new java.util.HashSet<>();

        for (int i = 0; i < dependenciesArray.length; i++)
        {
            String[] notation = dependenciesArray[i].split(":");
            SimpleDependency dependency = new SimpleExternalDependency(notation[0], notation[1], notation[2]);

            dependencySet.add(dependency); dependencies[i] = dependency;
            expectedDSLBlock[i] = dependency.toDSLDeclaration();
        }
        BuildFile result = buildWriter.declareExternalDependencies(dependencies).sign();

        assertEqualWriterSets(dependencySet, result.getDeclaredDependencies());
        validateBuildFileWriterDSLBlock(result, "dependencies", expectedDSLBlock);
    }

    @TestOnly
    private void validateBuildFileWriterDSLBlock(BuildFile result, String name, String[] expected) throws IOException {

        String[] dsl = constructDSLDeclarationBlock(name, expected);
        Assertions.assertEquals(Arrays.asList(dsl), FileUtils.readLines(result, CHARSET));
    }

    @TestOnly
    private String[] constructDSLDeclarationBlock(String name, String[] content) {

        String[] indented = Arrays.copyOf(content, content.length);
        /*
         * Indent each array element with '\t' to match how DSL
         * block declarations are constructed in BuildFile
         */
        for (int i = 0; i < content.length; i++) {
            indented[i] = '\t' + content[i];
        }
        String[] result = new String[content.length + 3];
        System.arraycopy(indented, 0, result, 1, content.length);

        result[0] = name + " {"; result[result.length - 2] = "}";
        result[result.length - 1] = ""; return result;
    }

    @TestOnly
    private <T> void assertEqualWriterSets(Set<T> expected, Set<T> actual) {
        Assertions.assertFalse(expected.retainAll(actual));
    }

    @Test
    void whenModifyWriterSetsShouldThrowUnsupportedOperationException() {

        BuildFile result = buildWriter.applyPlugins(CorePlugin.JAVA)
                .declareExternalDependencies(DUMMY_DEPENDENCY).sign();

        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> result.getDeclaredPlugins().add(new CommunityPlugin("dummy")));

        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> result.getDeclaredDependencies().add(DUMMY_DEPENDENCY));
    }

    @Test
    void shouldDeclareJUnitIntegrationDependencies() {

        BuildFile result = buildWriter.declareJUnitDependencies().sign();
        Set<SimpleDependency> dependencies = result.getDeclaredDependencies();

        Assertions.assertTrue(dependencies.contains(JUnitIntegration.API));
        Assertions.assertTrue(dependencies.contains(JUnitIntegration.ENGINE));
    }
}
