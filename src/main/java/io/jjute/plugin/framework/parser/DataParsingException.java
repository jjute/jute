package io.jjute.plugin.framework.parser;

/**
 * Signals that a fatal <i>(unrecoverable)</i> exception occurred while parsing
 * data using {@code DataParser}. This exception can be used to wrap checked
 * and unchecked exceptions to indicate that they caused a parsing failure.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class DataParsingException extends RuntimeException {

    /**
     * Create a new {@code DataParsingException} with a custom message and a reference
     * to a {@code Throwable} that should be considered the <i>cause</i> of this exception.
     */
    public DataParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create a new {@code DataParsingException} with a reference to a {@code Throwable}
     * that should be considered the <i>cause</i> of this exception.
     */
    public DataParsingException(Throwable cause) {
        super(cause);
    }
    /**
     * Create a new {@code DataParsingException} with a custom message.
     */
    public DataParsingException(String message) {
        super(message);
    }
}
