package io.jjute.plugin.framework.integration;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public class IntegrationModelTest {

    private static Project dummyProject;
    private DummyIntegrationModel model;

    @TestOnly
    private class DummyIntegrationModel extends IntegrationModel {

        private DummyIntegrationModel(String model, Project project) {
            super(model, project);
        }
    }
    @TestOnly
    private class DummyIntegrationException extends PluginIntegrationException {

        private DummyIntegrationException(IntegrationModel model, String cause) {
            super(model, cause);
        }
        private DummyIntegrationException(IntegrationModel model, Throwable cause) {
            super(model, cause);
        }
    }

    @BeforeAll
    public static void setupIntegrationModelTest() {
        ProjectBuilder builder =  ProjectBuilder.builder().withName("dummyProject");
        dummyProject = Objects.requireNonNull(builder.build());
    }

    @BeforeEach
    public void createDummyIntegrationModel() {
        model = new DummyIntegrationModel("dummy", dummyProject);
    }

    @Test
    public void shouldReturnValidValuesFromGetters() {

        Assertions.assertEquals(dummyProject, model.getProject());
        Assertions.assertEquals("dummy", model.getModelName());
    }

    @Test
    public void shouldProduceExceptionWithValidMessage() {

        final String cause = "Unknown cause";
        Exception e = new Exception();

        assertValidExceptionMessage(new DummyIntegrationException(model, cause), cause);
        assertValidExceptionMessage(new DummyIntegrationException(model, e), "");
    }

    @TestOnly
    private void assertValidExceptionMessage(DummyIntegrationException e, String cause) {

        String regexFormat = "^%s\\s.*\\s%s\\s.*\\s%s$";
        String regex = String.format(regexFormat, model.getModelName(), model.getProject().getName(), cause);
        Assertions.assertTrue(java.util.regex.Pattern.compile(regex).matcher(e.getMessage()).find());
    }
}
