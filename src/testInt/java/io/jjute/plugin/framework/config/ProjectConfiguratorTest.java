package io.jjute.plugin.framework.config;

import io.jjute.plugin.framework.*;
import io.jjute.plugin.framework.define.CommonRepository;
import io.jjute.plugin.framework.util.TaskUtils;
import io.jjute.plugin.testsuite.core.BuildTest;
import io.jjute.plugin.testsuite.file.BuildFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.gradle.api.tasks.testing.logging.TestExceptionFormat;
import org.gradle.api.tasks.testing.logging.TestLoggingContainer;
import org.gradle.testfixtures.ProjectBuilder;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

class ProjectConfiguratorTest extends BuildTest {

    private static final String DUMMY_UNIT_TEST_FILE = "DummyUnitTest.java";
    private static final String DUMMY_UNIT_TEST_PATH = "src/test/java/io/jjute/test";
    /*
     * New FunctionTest instance is created for each test so all
     * non-static fields here will be null until test initialization
     * method setupProjectConfiguratorProjectTest is called
     */
    private @Nullable PluginConfig config;
    private @Nullable org.gradle.api.tasks.testing.Test test;
    private @Nullable ProjectConfigurator configurator;

    /**
     * Call this method before any test that requires a {@code Project} and
     * {@code ProjectConfigurator} instance. Note that a new FunctionTest instance
     * is created for each test so all <i>non-static</i> class fields will be null
     * until this method has been called at the start of each relevant test.
     */
    @TestOnly
    void setupProjectConfiguratorProjectTest() {

        org.gradle.api.Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("java");
        config = JutePlugin.createPluginConfig(project);

        test = TaskUtils.getTestTask(project);
        configurator = ProjectConfigurator.create(project, config);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void shouldConfigureTestTaskProperties() {

        setupProjectConfiguratorProjectTest();
        TestLoggingContainer logging = test.getTestLogging();
        /*
         * Set all relevant Test task configurations opposite of their respected PluginConfig
         * values so that we can validate if the configuration process was successful
         */
        test.setFailFast(!config.testFailFast);

        logging.setShowStackTraces(!config.testShowStackTraces);
        logging.setExceptionFormat(config.testExceptionFormat.equalsIgnoreCase("FULL") ?
                TestExceptionFormat.SHORT : TestExceptionFormat.FULL);

        java.util.stream.Stream<Object> dependsOn = test.getDependsOn().stream();
        if (dependsOn.anyMatch(t -> t.toString().equals("cleanTest"))) {
            test.setDependsOn(dependsOn.filter(t -> !t.toString().equals("cleanTest"))
                    .collect(java.util.stream.Collectors.toSet()));
        }
        /*
         * Make sure that all values have been inverted or otherwise changed
         */
        Assertions.assertNotEquals(config.testFailFast, test.getFailFast());
        Assertions.assertNotEquals(config.testShowStackTraces, logging.getShowStackTraces());
        Assertions.assertNotEquals(TestExceptionFormat.valueOf(
                config.testExceptionFormat), logging.getExceptionFormat());

        Assertions.assertNotEquals(1, test.getDependsOn().stream()
                .filter(t -> t.toString().equals("cleanTest")).count());
        /*
         * Configure sample test task here
         */
        configurator.withTestTasks("test").configure();
        /*
         * Confirm that task properties have been updated to match config values
         */
        Assertions.assertEquals(config.testFailFast, test.getFailFast());
        Assertions.assertEquals(1, test.getDependsOn().stream()
                .filter(t -> t.toString().equals("cleanTest")).count());

        Assertions.assertEquals(TestExceptionFormat.valueOf(config.testExceptionFormat), logging.getExceptionFormat());
        Assertions.assertEquals(config.testShowStackTraces, logging.getShowStackTraces());
    }

    @Test
    void shouldBuildWithJUnitIntegrationAndRunTest() throws IOException {

        BuildFile.create(buildDir).applyPlugins(CorePlugin.JAVA)
                .addRepositories(CommonRepository.JCENTER).withJUnitIntegration().sign();

        File dummyTest = copyResourceToDirectory(DUMMY_UNIT_TEST_FILE, DUMMY_UNIT_TEST_PATH);
        setupAndRunDummyUnitTest(dummyTest, null);
    }

    @Test
    void whenRunningTestsShouldProduceLogging() throws IOException {

        BuildFile.create(buildDir).applyPlugins(getDevelopingPlugin()).sign();

        File dummyTestFile = copyResourceToDirectory(DUMMY_UNIT_TEST_FILE, DUMMY_UNIT_TEST_PATH);
        File logFile = buildDir.toPath().resolve("dummy.log").toFile();
        setupAndRunDummyUnitTest(dummyTestFile, logFile,"-i");

        String dummyTestCode = FileUtils.readFileToString(dummyTestFile, Charset.defaultCharset());
        int testCount = getNumberOfRegexMatches("@Test\\s", dummyTestCode);
        Assertions.assertTrue(testCount > 0);

        String logFileText = FileUtils.readFileToString(logFile, Charset.defaultCharset());

        @RegExp String logLineRegex = "Running\\stest:\\sTest\\s.*\\(\\)\\([a-zA-Z.]+\\)";
        Assertions.assertEquals(testCount,getNumberOfRegexMatches(logLineRegex, logFileText));
    }

    @TestOnly
    private void setupAndRunDummyUnitTest(File test, @Nullable File logFile, String... arguments) throws IOException {

        if (logFile != null)
        {
            if (!logFile.exists() && !logFile.createNewFile()) {
                throw new IOException("Unable to create new \"dummy.log\" file.");
            }
            /* Clear the logfile before writing to it if it's not empty.
             * This should not happen as we should run separate test instance
             * for each method but keep this just as a precaution
             */
            else if (FileUtils.readLines(logFile, Charset.defaultCharset()).size() > 0) {
                FileUtils.writeByteArrayToFile(logFile, new byte[0]);
            }
        }
        try (PrintStream stream = logFile != null ? new PrintStream(logFile) : null)
        {
            PrintStream sysStream = System.out;
            if (stream != null) {
                System.setOut(stream);      // Reassigns the "standard" output stream to dummy.log file
            }
            Assertions.assertTrue(test.exists());
            createRunnerForPlugin().withArguments(ArrayUtils.addAll(new String[]{"test"}, arguments)).build();

            for (String dummy : new String[] { ".firstTest", ".secondTest", ".thirdTest" }) {
                Assertions.assertTrue(buildDir.toPath().resolve("build/dummy/" + dummy).toFile().exists());
            }
            if (stream != null) {
                System.setOut(sysStream);   // Reassigns output stream from file back to console stream
            }
        }
    }

    @TestOnly
    private static int getNumberOfRegexMatches(String regex, String input) {

        int matches = 0; Pattern pattern = Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(input);
        while (matcher.find()) matches++; return matches;
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void whenConfiguringProjectMultipleTimesShouldThrowException() throws Throwable {

        setupProjectConfiguratorProjectTest();
        Executable configure = () -> configurator.configure(); configure.execute();
        Assertions.assertThrows(UnsupportedOperationException.class, configure);
    }
}
