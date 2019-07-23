package io.jjute.plugin.framework.integration;

import io.jjute.plugin.framework.PluginExecutionException;
import org.gradle.api.Project;

/**
 * Signals that a fatal exception occurred while integrating a Gradle plugin system.
 */
@SuppressWarnings("WeakerAccess")
public class PluginIntegrationException extends PluginExecutionException {

    private static final String message = "integration failed for project %s with an exception.";

    /**
     * Create a new {@code PluginIntegrationException} for the given model name and
     * {@code Project} with a log message that describes the cause of this exception.
     */
    protected PluginIntegrationException(String model, Project project, String cause) {
        super(String.format(model + ' ' + message + ' ' + cause, project.getName()));
    }

    /**
     * Create a new {@code PluginIntegrationException} for the given {@code IntegrationModel}
     * with a log message that describes the cause of this exception.
     */
    protected PluginIntegrationException(IntegrationModel model, String cause) {
        this(model.getModelName(), model.getProject(), cause);
    }

    /**
     * Create a new {@code PluginIntegrationException} for the given model name and {@code Project} with
     * a reference to a {@code Throwable} that should be considered the <i>cause</i> of this exception.
     */
    protected PluginIntegrationException(String model, Project project, Throwable cause) {
        super(String.format(model + ' ' + message + ' ', project.getName()), cause);
    }

    /**
     * Create a new {@code PluginIntegrationException} for the given model name with a reference
     * to a {@code Throwable} that should be considered the <i>cause</i> of this exception.
     */
    protected PluginIntegrationException(IntegrationModel model, Throwable cause) {
        this(model.getModelName(), model.getProject(), cause);
    }
}
