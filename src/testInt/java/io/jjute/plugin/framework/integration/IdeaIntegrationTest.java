package io.jjute.plugin.framework.integration;

import io.jjute.plugin.testsuite.core.BuildTest;
import org.junit.jupiter.api.Test;
import java.io.IOException;

import static io.jjute.plugin.framework.PluginConfig.Property;

class IdeaIntegrationTest extends BuildTest {

    private final String IDEA_INTEGRATION = Property.IDEA_INTEGRATION.getName();

    @Test
    void whenIdeaIntegrationEnabledShouldApplyIdeaPlugin() throws IOException {

        initializeBuild(new String[] {
                "task validateIdeaIntegration {",
                "   if (!project.plugins.hasPlugin('idea'))",
                "       throw new RuntimeException('Unable to find idea plugin')",
                "}"
        }); createRunnerForPlugin().withProperty(IDEA_INTEGRATION, "false").buildAndFail();
        createRunnerForPlugin().withProperty(IDEA_INTEGRATION, "true").build();
    }

    @Test
    void whenIdeaIntegrationEnabledShouldFindPlugin() throws IOException {

        initializeBuild(new String[] {
            "task validateIdeaIntegration {",
            "   IdeaPlugin ideaPlugin = plugins.getPlugin('idea')",
            "   if (ideaPlugin == null || ideaPlugin.getModel().module == null)",
            "       throw new RuntimeException(\"Unable to find IDEA plugin or module.\")",
            "}"
        }); createRunnerForPlugin().withProperty(IDEA_INTEGRATION, "true").build();
    }

    @Test
    void shouldConfigureValidOutputPaths() throws IOException {

        java.util.Map<String, Object> properties = new java.util.HashMap<>();

        String outputDirPath = "build/target/production/box";
        String testOutputDirPath = "build/target/test/box";

        properties.put(IDEA_INTEGRATION, true);
        properties.put(Property.IDEA_INHERIT_DIRS.getName(), false);
        properties.put(Property.IDEA_OUTPUT_DIR.getName(), outputDirPath);
        properties.put(Property.IDEA_TEST_OUTPUT_DIR.getName(), testOutputDirPath);

        writeToGradleProperties(properties);
        initializeBuild(new String[] {
                "task validateIdeaOutputPaths {",
                "   String outputDirPath = idea.module.outputDir.path",
                "   if (System.getProperty(\"os.name\").startsWith(\"Windows\")) {",
                "       outputDirPath = outputDirPath.replace(\"\\\\\", \"/\")",
                "   }",
                "   if (!outputDirPath.equals(\"" + outputDirPath + "\")) {",
                "       throw new RuntimeException(\"Invalid IDEA outputDir path: $outputDirPath " +
                                "expected: " + outputDirPath + "\")",
                "   }",
                "   String testOutputDirPath = idea.module.testOutputDir.path",
                "   if (System.getProperty(\"os.name\").startsWith(\"Windows\")) {",
                "       testOutputDirPath = testOutputDirPath.replace(\"\\\\\", \"/\")",
                "   }",
                "   if (!testOutputDirPath.equals(\"" + testOutputDirPath + "\")) {",
                "       throw new RuntimeException(\"Invalid IDEA testOutputDir path: $testOutputDirPath\")",
                "   }",
                "}"
        }); createRunnerForPlugin().build();
    }
}
