package io.jjute.plugin.framework;

import io.jjute.plugin.testsuite.core.FunctionalTest;
import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

class PluginConfigTest extends FunctionalTest {

    private static final Map<String, Object> PROPERTIES_OBJECTS = getGradlePropertiesAsObjects();
    private static final Map<String, String> PROPERTIES_LITERALS = getLiteralGradleProperties();
    private static final Map<String, String> PROPERTIES_QUOTED = getQuotedGradleProperties();

    @Test
    void shouldSetConfigsFromGradleProperties() throws IOException {

        writeToGradleProperties(PROPERTIES_OBJECTS);
        initializeBuild(getCompareConfigsTaskBuildLines());
        createRunnerForPlugin().build();
    }

    @Test
    void shouldSetConfigsFromCommandArguments() {

        initializeBuild(getCompareConfigsTaskBuildLines());
        createRunnerForPlugin().withProperties(PROPERTIES_LITERALS).build();
    }

    @TestOnly
    private String[] getCompareConfigsTaskBuildLines() {

        java.util.List<String> buildLines = new java.util.ArrayList<>();
        buildLines.add("java.util.Map<Object, Object> initializeData() {");
        buildLines.add("   java.util.Map<Object, Object> data = new java.util.HashMap()");

        for (Map.Entry<String, String> entry : PROPERTIES_QUOTED.entrySet()) {
            buildLines.add(String.format("   data.put(%s, %s)", entry.getValue(), entry.getKey()));
        }
        buildLines.add("   return data\n}");
        buildLines.addAll(java.util.Arrays.asList(
                "task compareConfigs {",
                "   java.util.Map<Object, Object> data = initializeData()",
                "   for (java.util.Map.Entry<Object, Object> entry : data.entrySet()) {",
                "       String property = String.valueOf(entry.getKey())",
                "       String config = String.valueOf(entry.getValue())",
                "       if (!property.equals(config)) {",
                "           String log = \"Property(%s) does not match configuration(%s).\"",
                "           throw new RuntimeException(String.format(log, property, config))",
                "       }",
                "   }",
                "}"
        )); return buildLines.toArray(new String[0]);
    }

    /**
     * @return Gradle properties in a {@code Map} where keys represents property
     *         names and values represent assertion expected {@code Object} values.
     */
    @TestOnly
    private static Map<String, Object> getGradlePropertiesAsObjects() {

        Map<String, Object> map = new java.util.LinkedHashMap<>();

        map.put("projectJavaVersion", 1.1);
        map.put("isProjectJavaLibrary", false);
        map.put("enableIDEAIntegration", false);
        map.put("ideaOutputDir", "test/directory/path/one");
        map.put("ideaTestOutputDir", "test/directory/path/two");
        map.put("enableJUnitIntegration", true);
        map.put("testShowStackTraces", true);
        map.put("testExceptionFormat", "FULL");
        map.put("testFailFast", false);

        return map;
    }

    /**
     * @param quoteStrings if {@code true} property values recognized as {@code String}
     *                    objects will be encapsulated with quotation marks.
     * @return Gradle properties in a {@code Map} where keys represents property
     *         names and values represent assertion expected {@code String} values.
     */
    @TestOnly
    private static Map<String, String> getGradlePropertiesAsStrings(boolean quoteStrings) {

        Map<String, String> map = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : getGradlePropertiesAsObjects().entrySet())
        {
            Object value = entry.getValue();
            String sValue = String.valueOf(value);
            boolean isString = value instanceof String;

            map.put(entry.getKey(), quoteStrings && isString ? '\"' + sValue + '\"' : sValue);
        }
        return map;
    }

    private static Map<String, String> getQuotedGradleProperties() {
        return getGradlePropertiesAsStrings(true);
    }

    private static Map<String, String> getLiteralGradleProperties() {
        return getGradlePropertiesAsStrings(false);
    }
}
