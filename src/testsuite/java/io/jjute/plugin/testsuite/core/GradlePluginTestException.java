package io.jjute.plugin.testsuite;

/**
 * Signals that a fatal exception occurred while running a Gradle plugin test.
 * This should be thrown when the test execution state becomes unrecoverable due
 * to any of the following causes:
 * <ul>
 *     <li><b>A checked exception:</b> stated the checked exception as the cause.</li>
 *     <li><b>No exception:</b> the cause should be described in a log message.</li>
 * </ul>
 * This exception should <b>not</b> be thrown to handle unchecked exceptions.
 */
public class GradlePluginTestException extends RuntimeException {

    public GradlePluginTestException(String message) {
        super(message);
    }

    public GradlePluginTestException(String message, Throwable cause) {
        super(message, cause);
    }

    public GradlePluginTestException(String message, String arg, Throwable cause) {
        super(String.format(message, arg), cause);
    }
}
