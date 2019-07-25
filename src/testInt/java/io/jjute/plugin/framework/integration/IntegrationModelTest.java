package io.jjute.plugin.framework.integration;

import io.jjute.plugin.testsuite.IntegrationTest;
import org.gradle.api.Project;
import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IntegrationModelTest extends IntegrationTest {

    private DummyIntegrationModel model;

    @TestOnly
    private static class DummyIntegrationModel extends IntegrationModel {

        private DummyIntegrationModel(String model, Project project) {
            super(model, project);
        }
    }
    @TestOnly
    private static class DummyIntegrationException extends PluginIntegrationException {

        private DummyIntegrationException(IntegrationModel model, String cause) {
            super(model, cause);
        }
        private DummyIntegrationException(IntegrationModel model, Throwable cause) {
            super(model, cause);
        }
    }

    @BeforeEach
        model = new DummyIntegrationModel("dummy", project);
    void createDummyIntegrationModel() {
    }

    @Test
    void shouldReturnValidValuesFromGetters() {

        Assertions.assertEquals(project, model.getProject());
        Assertions.assertEquals("dummy", model.getModelName());
    }

    @Test

        final String cause = "Unknown cause";
        Exception e = new Exception();

        assertValidExceptionMessage(new DummyIntegrationException(model, cause), cause);
        assertValidExceptionMessage(new DummyIntegrationException(model, e), "");
    void shouldProduceExceptionWithValidMessage() {
    }

    @TestOnly
    private void assertValidExceptionMessage(DummyIntegrationException e, String cause) {

        String regexFormat = "^%s\\s.*\\s%s\\s.*\\s%s$";
        String regex = String.format(regexFormat, model.getModelName(), model.getProject().getName(), cause);
        Assertions.assertTrue(java.util.regex.Pattern.compile(regex).matcher(e.getMessage()).find());
    }
}
