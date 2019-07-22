package io.jjute.plugin.framework.io;

import org.jetbrains.annotations.NotNull;
import java.lang.reflect.InvocationTargetException;

/**
 * Parse {@code Object} or reference data types using external methods.
 * @see DataParser
 */
@SuppressWarnings("unchecked")
public class ObjectParser<R, I> extends DataParser<R, I> {

    public static final StringParser STRING = new StringParser();

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

    @Override
    public R parse(@NotNull I input) {
        try {
            return parseData(input);
        }
        catch (InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }
    @Override
    public String toString(Object type) {
        return type.toString();
    }
}
