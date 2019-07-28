package io.jjute.plugin.framework.util;

import io.jjute.plugin.testsuite.IntegrationTest;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.internal.project.taskfactory.TaskIdentity;
import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TaskUtilsTest extends IntegrationTest {

    @TestOnly
    public static class DummyTask extends DefaultTask {
        public DummyTask() {}
    }

    @Test
    void whenGettingTestTaskShouldNotThrowException() {
        Assertions.assertDoesNotThrow(() -> TaskUtils.getTestTask(project, "test"));
        Assertions.assertDoesNotThrow(() -> TaskUtils.getTestTask(project));
    }

    @Test
    void shouldReplaceExistingTaskWithNoArguments() {

        Task dummyTask = project.getTasks().create("dummyTask");
        Assertions.assertNotEquals(DummyTask.class, ((AbstractTask)dummyTask).getTaskIdentity().type);

        dummyTask = TaskUtils.replaceTask(project, "dummyTask", DummyTask.class);
        Assertions.assertEquals(DummyTask.class, ((AbstractTask)dummyTask).getTaskIdentity().type);

        Task foundTask = java.util.Objects.requireNonNull(project.getTasks().findByName("dummyTask"));
        TaskIdentity foundIdentity = ((AbstractTask)foundTask).getTaskIdentity();
        Assertions.assertEquals(((AbstractTask)dummyTask).getTaskIdentity(), foundIdentity);
    }
}
