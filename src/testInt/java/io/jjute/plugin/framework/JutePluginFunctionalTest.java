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

//    Project project = ProjectBuilder.builder().build();

    @Test
    public void shouldFailWhenProjectPluginNotFound() {

        initAndWriteToBuildFile(new String[] {
                "task verifyPlugin {",
                "   if (!project.plugins.hasPlugin('123abc348f'))",
                "       throw new RuntimeException()",
                "}"
        });
        createRunnerForPlugin().buildAndFail();
    }
}
