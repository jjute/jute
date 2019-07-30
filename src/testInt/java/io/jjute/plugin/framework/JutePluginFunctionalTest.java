package io.jjute.plugin.framework;

import io.jjute.plugin.testsuite.FunctionalTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class JutePluginFunctionalTest extends FunctionalTest {

    @Test
    void whenPropertiesAsOptionShouldApplyProjectPlugins() throws IOException {

        initializeBuildFile(new String[] {
                "task verifyPlugin {",
                "   projectPlugins.split(',').each { plugin ->",
                "       if (!project.plugins.hasPlugin(plugin))",
                "           throw new RuntimeException(\"Missing project plugin: \" + plugin)",
                "   }",
                "}"
        }); String[] arguments = { CorePlugin.IDEA.getId(), CorePlugin.JAVA.getId() };
        createRunnerForPlugin().withProperty("projectPlugins", arguments).build();
    }

    @Test
    void shouldFailWhenProjectPluginNotFound() throws IOException {

        initializeBuildFile(new String[] {
                "task verifyPlugin {",
                "   if (!project.plugins.hasPlugin('123abc348f'))",
                "       throw new RuntimeException()",
                "}"
        }); createRunnerForPlugin(false, true).buildAndFail();
    }
    
    @Test
    void shouldAddJCenterRepository() throws IOException {

        initializeBuildFile(new String[] {
                "task verifyRepository {",
                "    if (project.getRepositories().findByName('BintrayJCenter') == null)",
                "        throw new RuntimeException()",
                "}"
        }); createRunnerForPlugin().build();
    }

    @Test
    void shouldUseCorrectJavaVersionCompatibility() throws IOException {

        initializeBuildFile(new String[] {
                "task verifyCompatibility {",
                "   String sVersion = findProperty('projectJavaVersion')",
                "   JavaVersion version = JavaVersion.toVersion(sVersion)",
                "   JavaVersion srcComp = getSourceCompatibility()",
                "   JavaVersion targetComp = getTargetCompatibility()",
                "   if (!version.equals(srcComp) || !version.equals(targetComp))",
                "       throw new RuntimeException()",
                "}"
        }); createRunnerForPlugin().build();
    }
}
