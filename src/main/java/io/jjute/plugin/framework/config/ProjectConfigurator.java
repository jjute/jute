package io.jjute.plugin.framework.config;

import io.jjute.plugin.framework.PluginConfig;
import io.jjute.plugin.framework.util.TaskUtils;
import org.gradle.api.Project;
import org.gradle.api.tasks.testing.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.Callable;

/**
 * An object designed to configure various Gradle {@code Project} aspects in a modular
 * multi-threaded environment. The configuration process stores each configuration task
 * as a {@link ConfigRunnable} which are invoked at the end of the configuration process.
 * <ul>
 *     <li>
 *         Use {@link #create(Project, PluginConfig)} method to create a new  {@code ProjectConfigurator}
 *         instance which can then be used to configure the specified {@code Project} employing a
 *         builder design pattern that stores each configuration action as a {@code Runnable} task.
 *     <li>
 *         Use {@link #configure()} method to end the configuration process where each stored
 *        {@link ConfigRunnable} instance will be concurrently invoked by {@code ThreadPoolExecutor}
 *        which will cause the main thread to wait for all tasks to finish their work before returning.
 *     </li>
 * </ul>
 * This system was originally intended to handle Groovy closures which are tricky to handle in
 * regular Java and this is why {@code ConfigRunnable} implementations are written in Groovy.
 * However Java task implementations are permitted and as long as the task configures the
 * project in some way and is not needed to run more then <i>one-time</i> it fits the bill.
 */
public class ProjectConfigurator {

    protected final Project project;
    protected final PluginConfig config;
    private boolean isConfigured;

    private java.util.concurrent.ExecutorService service = Executors.newCachedThreadPool();
    private java.util.List<ConfigRunnable> configRuns = new java.util.ArrayList<>();

    private ProjectConfigurator(Project project, PluginConfig config) {
        this.project = project;
        this.config = config;
    }

    /**
     * @param project {@code Project} instance to configure.
     * @param config plugin configuration values to use.
     * @return a new instance of {@code ProjectConfigurator}.
     */
    public static ProjectConfigurator create(Project project, PluginConfig config) {
        return new ProjectConfigurator(project, config);
    }

    /**
     * Configure test tasks that match the given name for the current project.
     *
     * @return instance of this {@code ProjectConfigurator}.
     * @see ConfigureTest#run()
     */
    public ProjectConfigurator withTestTasks(String... taskNames) {

        Test[] tests = new Test[taskNames.length];
        for (int i = 0; i < tests.length; i++) {
            tests[i] = TaskUtils.getTestTask(project, taskNames[i]);
        }
        return submitRunnable(new ConfigureTest(this, tests));
    }

    /**
     * Run all registered configuration actions defined as <code>Runnable</code> objects
     * using an internal {@code ExecutorService}. Note that this method <b>cannot</b> be
     * called multiple times on the same <code>ProjectConfigurator</code> instance, and
     * doing so will result in a <code>UnsupportedOperationException</code> exception.
     */
    public void configure() {
        if (isConfigured) {
            throw new UnsupportedOperationException(String.format("Project %s has already been " +
                    "configured with this instance of ProjectConfigurator.", project.getName()));
        }
        java.util.List<Callable<Object>> calls = new java.util.ArrayList<>();
        configRuns.forEach(r -> calls.add(Executors.callable(r)));
        try {
            service.invokeAll(calls);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        isConfigured = true;
        service.shutdown();
    }

    /**
     * Internal method that stores the given {@code ConfigRunnable} in a {@code List} that will
     * be executed when {@link #configure()} method is called on this {@code ProjectConfigurator}.
     *
     * @return instance of this {@code ProjectConfigurator}.
     */
    private ProjectConfigurator submitRunnable(ConfigRunnable runnable) {
        configRuns.add(runnable);
        return this;
    }
}
