package io.jjute.plugin.framework.util;

import io.jjute.plugin.testsuite.IntegrationTest;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProjectDependencyTest extends IntegrationTest {

    private static final String apacheLang = "org.apache.commons:commons-lang3:3.9";
    private static final String apacheMath = "org.apache.commons:commons-math3:3.6.1";

    @BeforeEach
    void addSampleApacheDependencies() {

        DependencyHandler handler = project.getDependencies();
        handler.add("implementation", apacheLang);
        handler.add("implementation", apacheMath);
    }

    @Test
    void whenGettingTestTaskShouldNotThrowException() {
        Assertions.assertDoesNotThrow(() -> ProjectUtils.getTestTask(project));
    }

    @Test
    void shouldFindDeclaredProjectDependencies() {

        String g1, n1, v1, g2, n2, v2;

        g1 = "org.apache.commons"; n1 = "commons-lang3"; v1 = "3.9";
        g2 = "org.apache.commons"; n2 = "commons-math3"; v2 = "3.6.1";

        Assertions.assertTrue(ProjectUtils.projectHasDependency(project, apacheLang));
        Assertions.assertTrue(ProjectUtils.projectHasDependency(project, apacheMath));

        Assertions.assertTrue(ProjectUtils.projectHasDependency(project, g1, n1, v1));
        Assertions.assertTrue(ProjectUtils.projectHasDependency(project, g2, n2, v2));
    }

    @Test
    void shouldConstructValidDependencyNotations() {

        java.util.Set<String> dependencyNotations = new java.util.HashSet<>();
        for (Dependency dependency : ProjectUtils.getProjectDependencies(project))
        {
            String notation = ProjectUtils.getDependencyNotation(dependency);
            /*
             * Confirm that both method return an identical result
             */
            Assertions.assertEquals(notation, ProjectUtils.getDependencyNotation(
                    dependency.getGroup(), dependency.getName(), dependency.getVersion()));

            dependencyNotations.add(notation);
        }
        Assertions.assertTrue(dependencyNotations.contains(apacheLang));
        Assertions.assertTrue(dependencyNotations.contains(apacheMath));
    }
}
