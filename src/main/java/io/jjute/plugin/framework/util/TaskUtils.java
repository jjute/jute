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
     * Create and replace an existing project {@link Task} with the specified name by adding it to the
     * given project's {@link TaskContainer} with the "{@link Task#TASK_OVERWRITE overwrite}" option.
     *
     * @param project {@code Project} recognized as the task's owner.
     * @param name name of the task to replace.
     * @param type {@code Class} of the new replacing task.
     * @param constructorArgs optional arguments for the custom class constructor we want to use to construct
     *                        the task. Note that if the nullary constructor is not available or accessible
     *                        and no constructor arguments are supplied the task will not be constructed.
     *
     * @return an instance of the replacing task.
     *
     * @throws NullPointerException if any of the values in parameter {@code constructorArgs} is {@code null}.
     * @throws ClassGenerationException if the type {@code Class} or it's nullary constructor have private access.
     *
     * @see TaskContainer#create(Map)
     */
    public static Task replaceTask(Project project, String name, Class<? extends AbstractTask> type, Object... constructorArgs) {

        java.util.Map<String, Object> map = new java.util.HashMap<>();

        map.put(Task.TASK_NAME, name); map.put(Task.TASK_OVERWRITE, "true"); map.put(Task.TASK_TYPE, type);
        map.put(Task.TASK_CONSTRUCTOR_ARGS, constructorArgs.length > 0 ? constructorArgs : null);

        return project.getTasks().create(map);
    }

    /**
     * @return {@link Test} task with the given name in charge of executing JUnit (3.8.x, 4.x or 5.x)
     *         or TestNG tests. Note that this task if only available if either {@code java}
     *         or {@code java-library} plugin has been applied to the given project.
     *
     * @throws UnknownTaskException if no {@code Test} task with the given name has been found for the
     *                              given project. The primary cause for this is the absence and application
     *                              of {@code java} and {@code java-library} plugin to the given project.
     */
    public static Test getTestTask(Project project, String name) {
        return project.getTasks().withType(Test.class).getByName(name);
    }
    /**
     * @return default {@link Test} task in charge of executing JUnit (3.8.x, 4.x or 5.x)
     *         or TestNG tests. Note that this task if only available if either {@code java}
     *         or {@code java-library} plugin has been applied to the given project.
     */
    public static Test getTestTask(Project project) {
        return getTestTask(project, "test");
    }
}
