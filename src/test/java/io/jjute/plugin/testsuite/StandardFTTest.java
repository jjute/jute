package io.jjute.plugin.testsuite;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.charset.Charset;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StandardFTTest extends FunctionalTest {

    private static final Charset CHARSET = Charset.defaultCharset();

    /**
     * <p>Test to see if {@code FunctionalTest} was setup correctly.
     */
    @Test @Order(1)
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

    @AfterEach
    void resetStandardFTTest() throws IOException {

        Assertions.assertTrue(buildFile.delete());
        Assertions.assertTrue(buildFile.createNewFile());
    }

    @Test
    void shouldClearAndInitializeBuildFile() throws IOException {

        FileUtils.write(buildFile, "text", CHARSET);
        String fileText =  FileUtils.readFileToString(buildFile, CHARSET);
        Assertions.assertEquals("text", fileText);

        initializeBuildFile();
        fileText =  FileUtils.readFileToString(buildFile, CHARSET);
        Assertions.assertNotEquals("text", fileText);
    }

    @Test
    void shouldWriteToBuildFile() throws IOException {

        String[] text = { "first line", "second line", "third line" };
        writeToBuildFile(text);

        java.util.List<String> textList = java.util.Arrays.asList(text);
        Assertions.assertFalse(FileUtils.readLines(buildFile, CHARSET).retainAll(textList));

        initAndWriteToBuildFile(text);
        Assertions.assertTrue(FileUtils.readLines(buildFile, CHARSET).retainAll(textList));
    }
}
