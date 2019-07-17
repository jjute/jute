package io.jjute.plugin.framework;

import io.jjute.plugin.testsuite.FunctionalTest;
import org.junit.jupiter.api.Test;

@SuppressWarnings("WeakerAccess")
public class JutePluginFunctionalTest extends FunctionalTest {

    @Test
    public void assertProjectPluginsApplied() {

        initAndWriteToBuildFile(new String[] {
                "task verifyPlugin {",
                "   projectPlugins.split(',').each { plugin ->",
                "       if (!project.plugins.hasPlugin(plugin))",
                "           throw new RuntimeException(\"Missing project plugin: \" + plugin)",
                "   }",
                "}"
        });

        String[] arguments = new String[JutePlugin.appliedPlugins.length];
        for (int i = 0; i < JutePlugin.appliedPlugins.length; i++) {
            arguments[i] = JutePlugin.appliedPlugins[i].getId();
        }
        String cmdArguments = "-PprojectPlugins=" + String.join(",", arguments);
        createRunnerForPlugin().withProperty("projectPlugins", arguments).build();
    }

    @Test
    public void assertNotFoundProjectPluginFails() {

        initAndWriteToBuildFile(new String[] {
                "task verifyPlugin {",
                "   if (!project.plugins.hasPlugin('123abc348f'))",
                "       throw new RuntimeException()",
                "}"
        });
        createRunnerForPlugin().buildAndFail();
    }
}
