package io.jjute.plugin.framework;

import io.jjute.plugin.testsuite.FunctionalTest;
import org.gradle.api.JavaVersion;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@SuppressWarnings("WeakerAccess")
public class ConventionConfigTest extends FunctionalTest {

    @Test
    public void shouldSetCorrectSourceAndTargetCompatibility() throws IOException {

        JavaVersion compatibility = JavaVersion.VERSION_1_9;

        java.util.Map<String, Object> properties = new java.util.HashMap<>();
        properties.put(PluginConfig.Property.JAVA_VERSION.getName(), compatibility);
        writeToGradleProperties(properties);

        initAndWriteToBuildFile(new String[] {
                "task verifyCompatibility {",
                "   JavaVersion compatibility = JavaVersion." + compatibility.name(),
                "   JavaPluginConvention jpConvention = project.convention.getPlugin(JavaPluginConvention.class)",
                "   if (jpConvention.targetCompatibility != compatibility || jpConvention.sourceCompatibility != compatibility)",
                "       throw new RuntimeException(\"Source or target compatibility does not match \" + compatibility.toString())",
                "}"
        }); createRunnerForPlugin().build();
    }

    @Test
    public void shouldSetCorrectSourceSetDirectoryLayout() {

        initAndWriteToBuildFile(new String[] {
            "void verifySourceSetDirectory(SourceDirectorySet set, String path) {",
            "   if (set.srcDirs.size() != 1 && !set.srcDirs.iterator().next().path.equals(path)) {",
            "       String message = \"SourceDirectorySet %s was not configured properly: %s\"",
            "       throw new RuntimeException(String.format(message, set.name, set.srcDirs.toString()))",
            "   }",
            "}\n",
            "sourceSets {",
            "   verifySourceSetDirectory(main.java, 'src/main/java')",
            "   verifySourceSetDirectory(main.resources, 'src/main/resources')",
            "   verifySourceSetDirectory(test.java, 'src/test/java')",
            "   verifySourceSetDirectory(test.resources, 'src/test/resources')",
            "}"
        }); createRunnerForPlugin().build();
    }
}
