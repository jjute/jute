package io.jjute.plugin.framework.parser;

import org.jetbrains.annotations.NotNull;
import java.lang.reflect.InvocationTargetException;

/**
 * Parse {@code Object} or reference data types using external methods.
 * @see DataParser
 */
@SuppressWarnings("unchecked")
public class ObjectParser<R, I> extends DataParser<R, I> {

    /**
     * @throws IllegalStateException if the resolved method is not declared static.
     * @throws IllegalArgumentException if the specified method was not found in the given type class.
     */
    public ObjectParser(Class<R> result, String method, Class<I> input) {
        super(result, method, input);
    }

    public ObjectParser(Class<R> result, String method) {
        this(result, method, (Class<I>) Object.class);
    }

    /**
     * @throws NullPointerException if the given object instance is {@code null}.
     * @throws DataParsingException when a {@link IllegalAccessException} is thrown when invoking the parsing method
     *                              because the access to method was denied or a {@link InvocationTargetException} is
     *                              thrown signaling that an underlying method of the invocation target threw an exception
     */
    @Override
    public R parse(@NotNull I input) {
        try {
            return parseData(input);
        }
        catch (InvocationTargetException e) {
            throw new DataParsingException(e);
        }
    }
}
