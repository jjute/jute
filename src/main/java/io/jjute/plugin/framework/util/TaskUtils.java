package io.jjute.plugin.framework.util;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.testing.Test;
import org.gradle.internal.instantiation.ClassGenerationException;

import java.util.Map;

public class TaskUtils {

    /**
     * @return {@link Test} task with the given name in charge of executing JUnit (3.8.x, 4.x or 5.x)
     *         or TestNG tests. Note that this task if only available if either {@code java} or
     *         {@code java-library} plugin has been applied to the given project.
     *
     * @throws UnknownTaskException if no {@code Test} task with the given name has been found for the
     *                              given project. The primary cause for this is the absence and application
     *                              of {@code java} and {@code java-library} plugin to the given project.
     */
    public static Test getTestTask(Project project, String name) {
        return project.getTasks().withType(Test.class).getByName(name);
    }
}
