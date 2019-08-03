package io.jjute.plugin.testsuite.file;

import io.jjute.plugin.framework.CommunityPlugin;
import io.jjute.plugin.framework.CorePlugin;
import io.jjute.plugin.framework.ProjectPlugin;
import io.jjute.plugin.framework.define.JuteDependency;
import io.jjute.plugin.framework.define.JuteExternalDependency;
import io.jjute.plugin.testsuite.core.UnitTest;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

class BuildFileTest extends UnitTest {

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

        List<String> expected = java.util.Arrays.asList(text);
        Assertions.assertEquals(expected, FileUtils.readLines(buildFile, CHARSET));
    }

    @Test
    void shouldConstructAndWriteDSLBlock() throws IOException {

        String[] content = { "\tfirst statement", "\tsecond statement", };
        String[] expectedDSLBlock = constructDSLDeclarationBlock("sampleBlock", content);

        BuildFile result = buildWriter.writeDSLBlock("sampleBlock", content).sign();

        List<String> expected = java.util.Arrays.asList(expectedDSLBlock);
        Assertions.assertEquals(expected, FileUtils.readLines(result, CHARSET));

        // Delete the build file before creating a new writer
        Assertions.assertTrue(result.delete());

        String line = "\t// do something here...";
        expectedDSLBlock = new String[]{ "task awesomeTask {", line, "}", "" };

        BuildFile.Writer newBuildWriter = BuildFile.create(buildDir);
        newBuildWriter.writeDSLBlock("task", "awesomeTask", new String[]{line});

        expected = java.util.Arrays.asList(expectedDSLBlock);
        Assertions.assertEquals(expected, FileUtils.readLines(newBuildWriter.sign(), CHARSET));
    }

    @Test
    void shouldApplyPluginsInDSLBlock() throws IOException {

        String[] expectedDSLBlock = {
                "\tid 'java'", "\tid 'idea'", "\tid \"com-pluginA\"",
                "\tid \"com-pluginB\" version \"1.0\""
        };
        ProjectPlugin[] pluginsArray = {
                CorePlugin.JAVA, CorePlugin.IDEA,
                new CommunityPlugin("com-pluginA"),
                new CommunityPlugin("com-pluginB", "1.0")
        };
        java.util.Set<ProjectPlugin> pluginsSet = new java.util.HashSet<>();
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

        JuteDependency[] dependencies = new JuteDependency[length];
        java.util.Set<JuteDependency> dependencySet = new java.util.HashSet<>();

        for (int i = 0; i < dependenciesArray.length; i++)
        {
            String[] notation = dependenciesArray[i].split(":");
            JuteDependency dependency = new JuteExternalDependency(notation[0], notation[1], notation[2]);

            dependencySet.add(dependency); dependencies[i] = dependency;
            expectedDSLBlock[i] = '\t' + dependency.toDSLDeclaration();
        }
        BuildFile result = buildWriter.declareExternalDependencies(dependencies).sign();

        assertEqualWriterSets(dependencySet, result.getDeclaredDependencies());
        validateBuildFileWriterDSLBlock(result, "dependencies", expectedDSLBlock);
    }

    @TestOnly
    private void validateBuildFileWriterDSLBlock(BuildFile result, String name, String[] expected) throws IOException {

        String[] dsl = constructDSLDeclarationBlock(name, expected);
        Assertions.assertEquals(java.util.Arrays.asList(dsl), FileUtils.readLines(result, CHARSET));
    }

    @TestOnly
    private String[] constructDSLDeclarationBlock(String name, String[] content) {

        String[] result = new String[content.length + 3];
        System.arraycopy(content, 0, result, 1, content.length);

        result[0] = name + " {"; result[result.length - 2] = "}";
        result[result.length - 1] = ""; return result;
    }

    @TestOnly
    private <T> void assertEqualWriterSets(java.util.Set<T> expected, java.util.Set<T> actual) {
        Assertions.assertFalse(expected.retainAll(actual));
    }

    @Test
    void whenModifyWriterSetsShouldThrowUnsupportedOperationException() {

        BuildFile result = buildWriter.applyPlugins(CorePlugin.JAVA)
                .declareExternalDependencies(JuteExternalDependency.DUMMY_DEPENDENCY).sign();

        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> result.getDeclaredPlugins().add(new CommunityPlugin("dummy")));

        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> result.getDeclaredDependencies().add(JuteExternalDependency.DUMMY_DEPENDENCY));
    }
}
