package io.jjute.plugin.framework.parser;

import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.validation.constraints.NotEmpty;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Parse primitive data types using external methods.
 * @see DataParser
 */
@SuppressWarnings("unchecked")
public class PrimitiveParser<R, I> extends DataParser<R, String> {

    public static final PrimitiveParser<Byte, Byte> BYTE = create(Byte.class);
    public static final PrimitiveParser<Short, Short> SHORT = create(Short.class);
    public static final PrimitiveParser<Integer, Integer> INTEGER = create(Integer.class);
    public static final PrimitiveParser<Long, Long> LONG = create(Long.class);
    public static final PrimitiveParser<Float, Float> FLOAT = create(Float.class);
    public static final PrimitiveParser<Double, Double> DOUBLE = create(Double.class);
    public static final PrimitiveParser<Boolean, Boolean> BOOLEAN = create(Boolean.class);

    public static final PrimitiveParser<Character, Character> CHARACTER = new PrimitiveParser(Character.class) {
        /**
         * @param text {@code String} value to convert to a {@code Character}.
         * @return the {@code char} representation of the given {@code String}. If the string is
         *         {@code null} or empty an appropriate exception is thrown, however if the string
         *         contains more then a single character then the first character is returned.

         * @throws IndexOutOfBoundsException if the given {@code String} is empty.
         */
        @Contract("null -> fail")
        public Character parse(@NotNull @NotEmpty String text) {
            return text.charAt(0);
        }
    };
    /**
     * Method used to preform the wrapping operation.
     * @see #wrap(Object)
     */
    private final Method wrapper;

    @SuppressWarnings("SameParameterValue")
    private PrimitiveParser(Class<R> result, String method) {
        super(result, method, String.class);
        wrapper = getWrapperMethod(result);
    }
    /**
     * This constructor should be used by {@code PrimitiveParser} implementations
     * that do not relay on delegating operations to methods resolved through
     * reflection but instead implement their own parsing system.
     *
     * @param result primitive data type {@code Class} that should be the result
     *               of the parsing operation custom implementation.
     */
    @SuppressWarnings({"WeakerAccess", "SameParameterValue"})
    protected PrimitiveParser(Class<R> result) {
        super(); wrapper = getWrapperMethod(result);
    }
    private static PrimitiveParser create(Class result) {
        return new PrimitiveParser(result, "valueOf");
    }
    private static Method getWrapperMethod(Class result) {
        Class primitive = ClassUtils.wrapperToPrimitive(result);
        try {
            return result.getDeclaredMethod("valueOf", primitive);
        }
        catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * @return a {@code String} representation of the given data.
     * @throws NullPointerException if the given object instance is {@code null}.
     * @throws DataParsingException when a {@link IllegalAccessException} is thrown when invoking the parsing method
     *                              because the access to method was denied or a {@link InvocationTargetException} is
     *                              thrown signaling that an underlying method of the invocation target threw an exception
     */
    @Contract("null -> fail")
    public R parse(@NotNull String text) {

        try {
            return parseData(text);
        }
        catch (InvocationTargetException e) {
            throw new DataParsingException(e);
        }
    }

    /**
     * <p>
     *     Wrap the given object of primitive data type {@code I} to a designated result data
     *     type {@code R}. Calling this method is the equivalent of calling {@code valueOf(I)}
     *     method of resulting data type {@code R} which according to documentation should
     *     generally be used in preference primitive data type constructor as it is likely
     *     to yield significantly better space and time performance.
     * <p>
     *     Also note the this method offers a much safer way of parsing then {@link #parseObject(Object)}
     *     and should always be preferred over the alternative unless the data type is not known during
     *     compile time in which case we have to pass an object.
     * </p>
     * @param primitive object instance of primitive type to wrap to {@code I}.
     * @return the result of wrapping the given object of data type {@code I} in data type {@code R}.
     *
     * @throws NullPointerException if the given object instance is {@code null}.
     * @throws DataParsingException when a {@link IllegalAccessException} is thrown when invoking parsing method
     *                               because the access to method was denied or a {@link InvocationTargetException}
     *                               is thrown from an underlying method when invoking the parsing method.
     *                               Exception cause will always be properly set to one of the mentioned methods.
     */
    public R wrap(@NotNull I primitive) {

        try {
            return (R) wrapper.invoke(null, primitive);
        }
        catch (InvocationTargetException | IllegalAccessException e) {
            throw new DataParsingException(e);
        }
    }
}
