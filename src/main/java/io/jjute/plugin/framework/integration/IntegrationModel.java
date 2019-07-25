package io.jjute.plugin.framework.integration;

import org.gradle.api.Project;

@SuppressWarnings("WeakerAccess")
public class IntegrationModel {

    private final String model;
    protected final Project project;

    protected IntegrationModel(String model, Project project) {
        this.model = model;
        this.project = project;
    }

    public String getModelName() {
        return model;
    }
    public Project getProject() {
        return project;
    }
}
