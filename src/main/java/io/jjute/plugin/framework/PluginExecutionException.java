package io.jjute.plugin.framework;

/**
 * Signals that a fatal <i>(unrecoverable)</i> exception occurred while
 * applying or more broadly executing a community Gradle plugin.
 */
public class PluginExecutionException extends RuntimeException {

    /**
     * Create a new {@code PluginExecutionException} with the given message and a reference
     * to a {@code Throwable} that should be considered the <i>cause</i> of this exception.
     */
    public PluginExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
    /**
     * Create a new {@code PluginExecutionException} with the given message.
     */
    public PluginExecutionException(String message) {
        super(message);
    }
    /**
     * Create a new {@code PluginExecutionException} with a reference to a
     * {@code Throwable} that should be considered the <i>cause</i> of this exception.
     */
    public PluginExecutionException(Throwable cause) {
        super(cause);
    }
}
