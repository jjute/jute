package io.jjute.plugin.framework.integration;

import io.jjute.plugin.framework.PluginConfig;
import io.jjute.plugin.testsuite.core.BuildTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JUnitIntegrationTest extends BuildTest {

    private static final String JUNIT_INTEGRATION = PluginConfig.Property.JUNIT_INTEGRATION.getName();

    @Test
    void shouldAddProjectDependencies() {

        initializeBuild(new String[] {
                "task verifyJUnitIntegration {",
                "   if (!(test.getTestFramework() instanceof org.gradle.api.internal.tasks.testing." +
                        "junitplatform.JUnitPlatformTestFramework))",
                "       throw new RuntimeException(\"JUnit was not configured as a project test framework.\")",
                "}",
        }); createRunnerForPlugin().withProperty(JUNIT_INTEGRATION, "true").build();
    }

    @Test
    void shouldConfigureProjectTestFramework() {

        Assertions.assertNotNull(JUnitIntegration.API);
        Assertions.assertNotNull(JUnitIntegration.ENGINE);

        initializeBuild(new String[] {
                "task verifyJUnitIntegration {",
                "   java.util.Set<Dependency> dependencies = io.jjute.plugin.framework.util." +
                        "DependencyUtils.getProjectDependencies(project)",
                "   io.jjute.plugin.framework.define.SimpleDependency[] junitDependencies = [",
                "       io.jjute.plugin.framework.integration.JUnitIntegration.API,",
                "       io.jjute.plugin.framework.integration.JUnitIntegration.ENGINE",
                "   ]",
                "   boolean[] foundDependency = new boolean[junitDependencies.length]",
                "   dependencies.each { d -> ",
                "       String notation = io.jjute.plugin.framework.util.DependencyUtils.getDependencyNotation(d)",
                "       for (int i = 0; i < foundDependency.length; i++) {",
                "           if (junitDependencies[i].getIdentifier().equals(notation))",
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
