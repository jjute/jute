package io.jjute.plugin.framework.integration;

import io.jjute.plugin.framework.PluginConfig;
import io.jjute.plugin.testsuite.core.FunctionalTest;
import org.gradle.api.JavaVersion;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class JavaIntegrationTest extends FunctionalTest {

    @Test
    void shouldSetSourceAndTargetCompatibility() throws IOException {

        JavaVersion compatibility = JavaVersion.VERSION_1_9;

        java.util.Map<String, Object> properties = new java.util.HashMap<>();
        properties.put(PluginConfig.Property.JAVA_VERSION.getName(), compatibility);
        writeToGradleProperties(properties);

        initializeBuild(new String[] {
                "task verifyCompatibility {",
                "   JavaVersion compatibility = JavaVersion." + compatibility.name(),
                "   JavaPluginConvention jpConvention = project.convention.getPlugin(JavaPluginConvention.class)",
                "   if (jpConvention.targetCompatibility != compatibility || jpConvention.sourceCompatibility != compatibility)",
                "       throw new RuntimeException(\"Source or target compatibility does not match \" + compatibility.toString())",
                "}"
        }); createRunnerForPlugin().build();
    }

    @Test
    void shouldSetSourceSetDirectoryLayout() throws IOException {

        initializeBuild(new String[] {
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

    @Test
    void shouldSetSingleSourceSetDirectory() throws IOException {

        initializeBuild(new String[] {
                "task setSingleSourceSetDir {",
                "   io.jjute.plugin.framework.integration.JavaIntegration java = " +
                        "new io.jjute.plugin.framework.integration.JavaIntegration(project)",
                "   SourceSet main = java.getConvention().getSourceSets().getByName(\"main\")",
                "   java.nio.file.Path path = java.nio.file.Paths.get('src/secondary/java')",
                "   java.setSingleSourceDir(main.getJava(), path.toString())",
                "   java.util.Set<File> srcDirs = sourceSets.main.java.srcDirs",
                "   java.nio.file.Path targetPath = projectDir.toPath().resolve(path)",
                "   if (srcDirs.size() != 1 || !srcDirs.iterator().next().toPath().equals(targetPath)) {",
                "       String message = \"Source dirs were not configured properly: %s, expected a single File entry %s.\"",
                "       throw new RuntimeException(String.format(message, srcDirs, targetPath))",
                "   }",
                "}"
        }); createRunnerForPlugin().build();
    }
}
