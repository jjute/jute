package io.jjute.plugin.framework.integration;

import io.jjute.plugin.framework.PluginConfig;
import io.jjute.plugin.testsuite.FunctionalTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class JUnitIntegrationTest extends FunctionalTest {

    private static final String JUNIT_INTEGRATION = PluginConfig.Property.JUNIT_INTEGRATION.getName();

    @Test
    void shouldAddProjectDependencies() throws IOException {

        initializeBuildFile(new String[] {
                "task verifyJUnitIntegration {",
                "   if (!(test.getTestFramework() instanceof org.gradle.api.internal.tasks.testing." +
                        "junitplatform.JUnitPlatformTestFramework))",
                "       throw new RuntimeException(\"JUnit was not configured as a project test framework.\")",
                "}",
        }); createRunnerForPlugin().withProperty(JUNIT_INTEGRATION, "true").build();
    }

    @Test
    void shouldConfigureProjectTestFramework() throws IOException {

        Assertions.assertNotNull(JUnitIntegration.API);
        Assertions.assertNotNull(JUnitIntegration.ENGINE);

        initializeBuildFile(new String[] {
                "task verifyJUnitIntegration {",
                "   java.util.Set<Dependency> dependencies = io.jjute.plugin.framework.util." +
                        "DependencyUtils.getProjectDependencies(project)",
                "   org.gradle.api.internal.artifacts.dependencies.DefaultClientModule[] junitDependencies = [",
                "       io.jjute.plugin.framework.integration.JUnitIntegration.API,",
                "       io.jjute.plugin.framework.integration.JUnitIntegration.ENGINE",
                "   ]",
                "   boolean[] foundDependency = new boolean[junitDependencies.length]",
                "   dependencies.each { d -> ",
                "       String notation = io.jjute.plugin.framework.util.DependencyUtils.getDependencyNotation(d)",
                "       for (int i = 0; i < foundDependency.length; i++) {",
                "           if (junitDependencies[i].getId().equals(notation))",
                "               foundDependency[i] = true",
                "       }",
                "   }",
                "   for (int i = 0; i < foundDependency.length; i++) {",
                "       if (foundDependency[i] == false)",
                "           throw new RuntimeException(\"Unable to find JUnit dependency: \" + junitDependencies[i])",
                "   }",
                "}"
        }); createRunnerForPlugin().build();
    }
}
