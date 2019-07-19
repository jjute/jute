package io.jjute.plugin.framework;

import io.jjute.plugin.testsuite.FunctionalTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@SuppressWarnings("WeakerAccess")
public class JutePluginFunctionalTest extends FunctionalTest {

    @Test
    public void whenPropertiesAsOptionShouldApplyProjectPlugins() throws IOException {

        initAndWriteToBuildFile(new String[] {
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
    public void shouldFailWhenProjectPluginNotFound() {

        initAndWriteToBuildFile(new String[] {
                "task verifyPlugin {",
                "   if (!project.plugins.hasPlugin('123abc348f'))",
                "       throw new RuntimeException()",
                "}"
        }); createRunnerForPlugin(false, true).buildAndFail();
    }
    
    @Test
    public void shouldAddJCenterRepository() {

        initAndWriteToBuildFile(new String[] {
                "task verifyRepository {",
                "    if (project.getRepositories().findByName('BintrayJCenter') == null)",
                "        throw new RuntimeException()",
                "}"
        }); createRunnerForPlugin().build();
    }

    @Test
    public void shouldUseCorrectJavaVersionCompatibility() {

        initAndWriteToBuildFile(new String[] {
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
