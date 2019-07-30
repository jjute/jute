package io.jjute.plugin.testsuite;

import io.jjute.plugin.framework.CommunityPlugin;
import io.jjute.plugin.framework.CorePlugin;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.gradle.testkit.runner.internal.DefaultGradleRunner;
import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StandardFTTest extends FunctionalTest {

    private static final Charset CHARSET = Charset.defaultCharset();

    @BeforeEach
    void shouldSetupStandardFTTest() throws IOException {

        Assertions.assertTrue(buildDir.isDirectory());
        Assertions.assertTrue(buildFile.exists());
        /*
         * Build file should contain no content.
         * This ensures that the file was freshly created.
         */
        String build = FileUtils.readFileToString(buildFile, CHARSET);
        Assertions.assertTrue(build.isEmpty());
    }

    @Test
    void shouldInitializeBuildFile() throws IOException {

        String[] plugins = { "plugins {", "\tid \"" + jutePlugin.getId() + "\"", "}", "" };
        String[] text = { "first line", "second line", "third line" };

        initializeBuildFile(text);

        List<String> textList = java.util.Arrays.asList(ArrayUtils.addAll(plugins, text));
        Assertions.assertEquals(textList, FileUtils.readLines(buildFile, CHARSET));
        Assertions.assertThrows(GradlePluginTestException.class, () -> initializeBuildFile(text));
    }

    @Test
    void shouldWriteToBuildFile() throws IOException {

        String firstLine = "first line\n";
        FileUtils.write(buildFile, firstLine, CHARSET);
        Assertions.assertEquals(firstLine, FileUtils.readFileToString(buildFile, CHARSET));

        String[] text = { "second line", "third line" };
        String[] expected = ArrayUtils.addAll(new String[] {firstLine.trim()}, text);

        writeToBuildFile(text);
        List<String> expectedList = java.util.Arrays.asList(expected);
        Assertions.assertEquals(expectedList, FileUtils.readLines(buildFile, CHARSET));
    }

    @Test
    void shouldFindAndReadPluginIdentifier() throws IOException {

        String identifier = "io.jjute.test-plugin";
        String implementationClass = "io.jjute.test.plugin.TestPlugin";
        String relativeIdPath = "src/main/resources/META-INF/gradle-plugins";

        File pluginIdDir = buildDir.toPath().resolve(relativeIdPath).toFile();
        Assertions.assertTrue(pluginIdDir.mkdirs());

        File pluginIdFile = pluginIdDir.toPath().resolve(identifier + ".properties").toFile();
        FileUtils.write(pluginIdFile, "implementation-class=" + implementationClass, CHARSET);
        Assertions.assertTrue(pluginIdFile.exists());

        File resources = SystemUtils.getUserDir().toPath().resolve("build/resources/main").toFile();
        List<File> classpath = new java.util.ArrayList<>(buildRunner.getPluginClasspath());

        /*
         * Confirm that resources/main is included in runner classpath and remove it.
         * This will remove the already existing plugin identifier from classpath
         */
        Assertions.assertTrue(classpath.contains(resources));
        Assertions.assertTrue(classpath.remove(resources));

        classpath.add(pluginIdDir);
        buildRunner.withPluginClasspath(classpath);
        Assertions.assertEquals(identifier, getPluginIdentifier());
    }

    @Test
    void shouldCreateFunctionalRunnerForPlugin() {

        DefaultGradleRunner defaultRunner = new DefaultGradleRunner();
        DefaultGradleRunner createdRunner = createRunnerForPlugin();

        Assertions.assertTrue(defaultRunner.getPluginClasspath().isEmpty());
        Assertions.assertFalse(createdRunner.getPluginClasspath().isEmpty());

        Assertions.assertNull(defaultRunner.getProjectDir());
        Assertions.assertEquals(buildDir, createdRunner.getProjectDir());

        Assertions.assertFalse(defaultRunner.isDebug());
        Assertions.assertTrue(createdRunner.isDebug());

        Assertions.assertFalse(isRunnerForwardingSystemStreams(defaultRunner));
        Assertions.assertTrue(isRunnerForwardingSystemStreams(createdRunner));
    }

    @TestOnly
    private boolean isRunnerForwardingSystemStreams(DefaultGradleRunner runner) {

        java.lang.reflect.Field forwardingStreams = Objects.requireNonNull(FieldUtils.getField(
                DefaultGradleRunner.class, "forwardingSystemStreams", true));
        try {
            forwardingStreams.setAccessible(true);
            return (boolean) forwardingStreams.get(runner);
        }
        catch (SecurityException | IllegalAccessException e) {
            throw new GradlePluginTestException("Unable to read " +
                    "\"forwardingSystemStreams\" field of DefaultGradleRunner.class", e);
        }
    }

    @Test
    void shouldLoadGradlePropertiesFromFile() throws IOException {

        File gradleProperties = getGradleProperties();
        Assertions.assertTrue(gradleProperties.exists());
        Assertions.assertNotNull(properties);

        List<String> propertyEntries = FileUtils.readLines(gradleProperties, CHARSET);
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String property = entry.getKey().toString() + '=' + entry.getValue().toString();
            Assertions.assertTrue(propertyEntries.contains(property));
        }
        // Compare the size in case properties field holds extra entries
        Assertions.assertTrue(propertyEntries.size() >= properties.entrySet().size());
    }

    @Test
    void shouldWriteToGradlePropertiesFile() throws IOException {

        File gradleProperties = getGradleProperties();
        Assertions.assertTrue(gradleProperties.exists());

        Assertions.assertThrows(InvalidPropertiesFormatException.class, () ->
                writeToGradleProperties(new String[] { "key1=value1", "key2=value2=x", "key3=value3" }));

        String[] propertiesArray = { "key1=value1", "key2=value2", "key3=value3", "key4=value4" };
        java.util.List<String> expectedResult = java.util.Arrays.asList(propertiesArray);

        Assertions.assertTrue(gradleProperties.delete());
        writeToGradleProperties(propertiesArray);
        Assertions.assertEquals(expectedResult, FileUtils.readLines(gradleProperties, CHARSET));

        java.util.Map<String, Object> propertiesMap = new java.util.HashMap<>();
        propertiesMap.put("key1", "value1"); propertiesMap.put("key2", "value2");
        propertiesMap.put("key3", "value3"); propertiesMap.put("key4", "value4");
        /*
         * Delete the file and check if it exists before writing values from array
         * to validate if the file was properly created via writeToGradleProperties(Map)
         */
        Assertions.assertTrue(gradleProperties.delete());
        writeToGradleProperties(propertiesMap);

        Assertions.assertTrue(gradleProperties.exists());
        writeToGradleProperties(propertiesArray);
        /*
         * Assert that writeToGradleProperties method will append content
         * to gradle.properties file rather then overwriting existing one
         */
        expectedResult = java.util.Arrays.asList(ArrayUtils.addAll(propertiesArray, propertiesArray));
        Assertions.assertEquals(expectedResult, FileUtils.readLines(gradleProperties, CHARSET));
    }

    @Test
    void whenApplyPluginsShouldDeclareDSLPluginsBlock() throws IOException {

        String[] expectedDSLBlock = {
                "plugins {", "\tid 'java'", "\tid 'idea'", "\tid \"com-pluginA\"",
                "\tid \"com-pluginB\" version \"1.0\"",  "}", ""
        };
        applyPlugins(CorePlugin.JAVA, CorePlugin.IDEA, new CommunityPlugin("com-pluginA"),
                new CommunityPlugin("com-pluginB", "1.0"));

        java.util.List<String> expected = java.util.Arrays.asList(expectedDSLBlock);
        Assertions.assertEquals(expected, FileUtils.readLines(buildFile, CHARSET));

        Assertions.assertThrows(GradlePluginTestException.class, () -> applyPlugins(CorePlugin.JAVA));
    }

    @Test
    void shouldCopyResourceFileToDestinationDirectory() {

        String resourcePath = "dummy.txt";
        String destinationPath = "src/test/java/io/jjute/test";

        File dummyUnitTestJava = copyResourceToDirectory(resourcePath, destinationPath);
        java.nio.file.Path expectedPath = java.nio.file.Paths.get(
                buildDir.toPath().toString(), destinationPath, resourcePath);

        Assertions.assertTrue(dummyUnitTestJava.exists());
        Assertions.assertEquals(expectedPath, dummyUnitTestJava.toPath());
    }
}
