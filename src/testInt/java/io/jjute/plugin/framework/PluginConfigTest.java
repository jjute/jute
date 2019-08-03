package io.jjute.plugin.framework;

import io.jjute.plugin.testsuite.core.FunctionalTest;
import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

class PluginConfigTest extends FunctionalTest {

    @Test
    void shouldSetConfigsFromGradleProperties() throws IOException {

        java.util.Map<String, Object> properties = new java.util.HashMap<>();

        properties.put("projectJavaVersion", 1.1);
        properties.put("isProjectJavaLibrary", false);
        properties.put("enableIDEAIntegration", false);
        properties.put("ideaOutputDir", "test/directory/path/one");
        properties.put("ideaTestOutputDir", "test/directory/path/two");

        writeToGradleProperties(properties);
        Map<String, String> map = new java.util.HashMap<>();
        /*
         * Keys represents plugin property names.
         * Values represent assertion expected values.
         */
        map.put("jute.projectJavaVersion", "JavaVersion.VERSION_1_1");
        map.put("jute.isProjectJavaLibrary", "false");
        map.put("jute.enableIDEAIntegration", "false");
        map.put("jute.ideaOutputDir", "\"test/directory/path/one\"");
        map.put("jute.ideaTestOutputDir", "\"test/directory/path/two\"");

        initializeBuildFile(getCompareConfigsTaskBuildLines(map));
        createRunnerForPlugin().build();
    }

    @Test
    void shouldSetConfigsFromCommandArguments() throws IOException {

        java.util.Map<String, Object> properties = new java.util.HashMap<>();

        properties.put("projectJavaVersion", 1.3);
        properties.put("isProjectJavaLibrary", false);
        properties.put("enableIDEAIntegration", true);
        properties.put("ideaOutputDir", "test/dir/path/one");
        properties.put("ideaTestOutputDir", "test/dir/path/two");

        writeToGradleProperties(properties);
        Map<String, String> map = new java.util.HashMap<>();
        /*
         * Keys represents plugin property names.
         * Values represent assertion expected values.
         */
        map.put("jute.projectJavaVersion", "JavaVersion.VERSION_1_3");
        map.put("jute.isProjectJavaLibrary", "false");
        map.put("jute.enableIDEAIntegration", "true");
        map.put("jute.ideaOutputDir", "\"test/dir/path/one\"");
        map.put("jute.ideaTestOutputDir", "\"test/dir/path/two\"");

        initializeBuildFile(getCompareConfigsTaskBuildLines(map));
    }

    @TestOnly
    private String[] getCompareConfigsTaskBuildLines(Map<String, String> map) {

        java.util.List<String> buildLines = new java.util.ArrayList<>();
        buildLines.add("java.util.Map<Object, Object> initializeData() {");
        buildLines.add("   java.util.Map<Object, Object> data = new java.util.HashMap()");

        for (Map.Entry<String, String> entry : map.entrySet()) {
            buildLines.add(String.format("   data.put(%s, %s)", entry.getValue(), entry.getKey()));
        }
        buildLines.add("   return data\n}");
        buildLines.addAll(java.util.Arrays.asList(
                "task compareConfigs {",
                "   java.util.Map<Object, Object> data = initializeData()",
                "   for (java.util.Map.Entry<Object, Object> entry : data.entrySet()) {",
                "       String property = entry.getKey(); String config = entry.getValue()",
                "       if (!property.equals(config)) {",
                "           String log = \"Property(%s) does not match configuration(%s).\"",
                "           throw new RuntimeException(String.format(log, property, config))",
                "       }",
                "   }",
                "}"
        )); return buildLines.toArray(new String[0]);
    }
}
