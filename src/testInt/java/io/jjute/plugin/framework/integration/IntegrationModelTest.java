package io.jjute.plugin.framework.integration;

import io.jjute.plugin.testsuite.core.IntegrationTest;
import org.gradle.api.Project;
import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IntegrationModelTest extends IntegrationTest {

    private static final String exceptionCause = "Unknown cause";
    private DummyIntegrationModel model;

    @TestOnly
    private static class DummyIntegrationModel extends IntegrationModel {

        private DummyIntegrationModel(Project project) {
            super("dummy", project);
        }
    }
    @TestOnly
    private static class DummyIntegrationException extends PluginIntegrationException {

        private DummyIntegrationException(IntegrationModel model) {
            super(model, exceptionCause);
        }
        private DummyIntegrationException(IntegrationModel model, Throwable cause) {
            super(model, cause);
        }
    }

    @BeforeEach
    void createDummyIntegrationModel() {
        model = new DummyIntegrationModel(project);
    }

    @Test
    void shouldReturnValidValuesFromGetters() {

        Assertions.assertEquals(project, model.getProject());
        Assertions.assertEquals("dummy", model.getModelName());
    }

    @Test
    void shouldProduceExceptionWithValidMessage() {
        
        assertValidExceptionMessage(new DummyIntegrationException(model), exceptionCause);
        assertValidExceptionMessage(new DummyIntegrationException(model, new Exception()), "");
    }

    @TestOnly
    private void assertValidExceptionMessage(DummyIntegrationException e, String cause) {

        String regexFormat = "^%s\\s.*\\s%s\\s.*\\s%s$";
        String regex = String.format(regexFormat, model.getModelName(), model.getProject().getName(), cause);
        Assertions.assertTrue(java.util.regex.Pattern.compile(regex).matcher(e.getMessage()).find());
    }
}
