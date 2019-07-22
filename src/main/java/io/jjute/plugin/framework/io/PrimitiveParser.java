package io.jjute.plugin.framework.io;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;

/**
 * Parse primitive data types using external methods.
 * @see DataParser
 */
@SuppressWarnings("unchecked")
public class PrimitiveParser<R, I> extends DataParser<R, I> {

    public static final PrimitiveParser<Byte, Byte> BYTE = Type.BYTE.parser;
    public static final PrimitiveParser<Short, Short> SHORT = Type.SHORT.parser;
    public static final PrimitiveParser<Integer, Integer> INTEGER = Type.INTEGER.parser;
    public static final PrimitiveParser<Long, Long> LONG = Type.LONG.parser;
    public static final PrimitiveParser<Float, Float> FLOAT = Type.FLOAT.parser;
    public static final PrimitiveParser<Double, Double> DOUBLE = Type.DOUBLE.parser;
    public static final PrimitiveParser<Boolean, Boolean> BOOLEAN = Type.BOOLEAN.parser;
    public static final PrimitiveParser<Character, Character> CHARACTER = Type.CHARACTER.parser;

    public enum Type {

        BYTE(Byte.class), SHORT(Short.class), INTEGER(Integer.class),
        LONG(Long.class), FLOAT(Float.class), DOUBLE(Double.class),
        BOOLEAN(Boolean.class), CHARACTER(Character.class);

        private final PrimitiveParser parser;
        Type(Class result) {

            if (result.isPrimitive()) {
                throw new IllegalArgumentException(String.format("Expected parameter(%s)" +
                        " to represents a non-primitive data type.", result.getName()));
            }
            @Nullable Class type;
            try {
                type = (Class) FieldUtils.getField(result, "TYPE").get(null);
            }
            catch (IllegalAccessException e) {
                throw new ExceptionInInitializerError(e);
            }
            if (type == null) {
                throw new ExceptionInInitializerError(new NoSuchMethodException(String.format(
                        "Unable to initialize PrimitiveParser.Type.%s. Expected to find TYPE " +
                                "field for primitive type " + result.getName(), this.name())));
            }
            else if (!type.isPrimitive()) {
                throw new IllegalArgumentException(String.format("Expected to find a " +
                        "primitive type instead of \"%s\".", type.getName()));
            }
            parser = new PrimitiveParser<>(result, type);
        }
    }
    private PrimitiveParser(Class<R> result, Class<I> input) {
        super(result, "valueOf", input);
    }
    /**
     * <p>
     *     Parse the given object of primitive data type {@code I} to a designated result data
     *     type {@code R}. Calling this method is the equivalent of calling {@code valueOf(I)}
     *     method of resulting data type {@code R} which according to documentation should
     *     generally be used in preference primitive data type constructor as it is likely
     *     to yield significantly better space and time performance.
     * <p>
     *     Also note the this method offers a much safer way of parsing then {@link #parseObject(Object)}
     *     and should always be preferred over the alternative unless the data type is not known during
     *     compile time in which case we have to pass an object.
     * </p>
     * @param primitive object instance of primitive type to parse to {@code I}.
     * @return the result of parsing given object of data type {@code I} to data type {@code R}.
     *
     * @throws NullPointerException if the given object instance is {@code null}.
     * @throws IllegalStateException when a {@link IllegalAccessException} is thrown when invoking parsing method
     *                               because the access to method was denied or a {@link InvocationTargetException}
     *                               is thrown from an underlying method when invoking the parsing method.
     *                               Exception cause will always be properly set to one of the mentioned methods.
     */
    @Contract("null -> fail")
    public R parse(@NotNull I primitive) {

        try {
            return parseData(primitive);
        }
        catch (InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    public String toString(I input) {
        return input.toString();
    }
}
