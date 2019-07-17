package io.jjute.plugin.framework;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.dsl.ScriptHandler;

public class JutePlugin implements Plugin<Project> {

    public static ProjectPlugin[] appliedPlugins = {
            CorePlugin.IDEA, CorePlugin.JAVA_LIBRARY
    };

    @Override
    public void apply(Project target) {

        ScriptHandler buildscript = target.getBuildscript();

        buildscript.getRepositories().gradlePluginPortal();
//        buildscript.getDependencies().add("classpath", "com.github.jk1:gradle-license-report:1.9");

        target.allprojects( project -> {

            for (ProjectPlugin plugin : appliedPlugins) {
                plugin.apply(project);
            }

            project.getRepositories().jcenter();

//            try {
//                project.getPluginManager().apply(Class.forName("com.github.jk1.license.LicenseReportPlugin"));
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            }
        });
    }
}
