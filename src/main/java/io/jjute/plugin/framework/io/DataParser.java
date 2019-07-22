package io.jjute.plugin.framework.io;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 *<p>
 *     Parse primitive and object data types using external methods.
 *<p>
 *     Methods used to parse the data are resolved with supplied parameters using reflection
 *     and invoked by calling {@link #parse(Object)} or {@link #parseObject(Object)}methods.
 *     The data parsing operations are delegated to these external methods, this class only
 *     handles storing and invoking method references with arguments and casting return values.
 * <dl>
 *   <dt>
 *       Here is an example of how to parse a primitive data type to {@code String}:
 *   <pre>
 *   DataParser{@code <String, Object>} parser = new ObjectParser<>(String.class, "valueOf", Double.TYPE);
 *   System.out.println(parser.parseType(10.25d)); // "10.25"
 *   </pre>
 *   <dt>
 *       Or we could use a {@link PrimitiveParser} which comes with pre-initialized parsers:
 *   </dt>
 *   <pre>
 *   System.out.println(PrimitiveParser.DOUBLE.toString(10.25d)); // "10.25"
 *   </pre>
 * <dt>
 *     Running the examples above would be the equivalent of calling {@link String#valueOf(double)},
 *     the latter of course being easier to handle and remember. Using {@code DataParser} however is a
 *     more generic approach to parsing values and allows us to parameterize and reuse parsing operations.
 *
 *     Take this example where we have multiple primitive data types in an Array or Collection of some sort.
 *     The standard approach would be to use conditional blocks to determine which parsing method we need
 *     to use and then invoke it on the proper class:
 *   <pre>
 *
 *   java.util.List{@code <Object> list = java.util.Arrays.asList(1, 1f, 1d);}
 *   list.forEach(System.out::println); // Object.toString()
 *   </pre>
 * <dt>
 *      Compare this to the use of {@code DataParser}:
 *   <pre>
 *
 *   java.util.Map{@code <Object, Class<?>> map = new java.util.HashMap<>();}
 *
 *   map.put(1, Integer.TYPE);
 *   map.put(1f, Float.TYPE);
 *   map.put(1d, Double.TYPE);
 *
 *   for (java.util.Map.Entry{@code <Object, Class<?>>} entry : map.entrySet()) {
 *       DataParser{@code <String, ?>} parser = new ObjectParser<>(String.class, "valueOf", entry.getValue());
 *       System.out.println(parser.parseObject(entry.getKey())) // String.valueOf(entry.getKey())
 *   }
 *   </pre>
 * <dt>
 *     As you can see in this particular example it is a lot easier to simply use {@link Object#toString()}
 *     which also has an advantage of being called automatically in many places where we would normally
 *     have to relay on explicitly calling a parsing method.
 *
 *     However {@code DataParser} is very useful when it comes to parsing data types to <i>other</i> data
 *     types that may or <i>may not</i> have parsing implementation methods for those types. This also
 *     includes custom data types as {@code DataParser} is primarily created as a way to handle them.
 *   <pre>
 *
 *   private static class MagicString {
 *
 *      final String value;
 *      private MagicString(String value) {
 *          this.value = '*' + value + '*';
 *      }
 *      public static MagicString valueOf(String value) {
 *          return new MagicString(value);
 *      }
 *      // Note this as a getter that acts like toString()
 *      public String customParsingMethod() {
 *          return value;
 *      }
 *  } // This is where the actual parsing happens
 *  DataParser{@code <MagicString, String>} parser;
 *  parser = new ObjectParser<>(MagicString.class, "valueOf", String.class);
 *
 *  MagicString parsed = parser.parse("text");         // new MagicString("text");
 *  System.out.println(parsed.customParsingMethod()); // *text*
 * </pre>
 * <p>
 *     Due to declaration brevity it is recommended to pre-declare and encapsulate
 *     object parsers you know you will end up using. Primitive parsers are already
 *     defined in {@code PrimitiveParser} within a static context for convenience.
 * </p>
 * @param <I> input data type used to invoke parsing method.
 * @param <R> data type that is the result of parse operation.
 */
@SuppressWarnings({"unchecked", "WeakerAccess"})
public abstract class DataParser<R, I> {

    /**
     * Method instance responsible for parsing data. It is resolved and
     * invoked through reflection. Currently it is required for the method to
     * contain a {@code static} modifier in order to be accessed, so methods
     * that are not declared {@code static} cannot be accessed here.
     */
    protected final Method method;

    /**
     * Resolve a {@code static} method with the given specifications.
     *
     * @param result the owner {@code Class} of the parsing method.
     * @param method simple name of the method to resolve.
     * @param input single method argument {@code Class}.
     *
     * @throws IllegalStateException if the resolved method is not declared static.
     * @throws IllegalArgumentException if the specified method was not found in the given type class.
     */
    protected DataParser(Class<R> result, String method, Class<I> input) {

        try {
            this.method = result.getDeclaredMethod(method, input);
            if (!Modifier.isStatic(this.method.getModifiers())) {
                throw new IllegalStateException(String.format("Expected found method %s " +
                        "to be declared static.", this.method.toGenericString()));
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Parse the given data of type {@code I} to data of type {@code R}.
     *
     * Note that implementations are free to handle parsing any way they like,
     * which does not necessarily involve the use of reflection to delegate.
     * In some situations this might be the preferred solution as can be seen
     * in {@link StringParser#parse(Object) StringParser}.
     *
     * @param data input data of type {@code I}.
     * @return output data of type {@code R}.
     */
    public abstract R parse(@NotNull I data);

    /**
     * @return a {@code String} representation of the given data.
     */
    public abstract String toString(I data);

    /**
     * Parse the given object of generic data type {@code I} to a designated result data type {@code R}.
     * This method offers a much safer way of parsing then {@link #parseObject(Object)} and should always
     * be preferred over the alternative unless the data type is not known during compile time.
     *
     * @param data object instance of generic data type {@code I} to parse to {@code R}.
     * @return the result of parsing given object of data type {@code I} to data type {@code R}.
     *
     * @throws NullPointerException if the given object instance is {@code null}.
     * @throws IllegalStateException when a {@link IllegalAccessException} is thrown when invoking
     *                               parsing method because the access to method was denied.
     */
    @Contract("null -> fail")
    protected R parseData(I data) throws InvocationTargetException {

        try {
            return (R) method.invoke(null, data);
        }
        catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Parse the given {@code Object} to a designated result data type {@code R}.
     * This method is generally not considered safe because it performs an unchecked
     * conversion operation to conform to {@link #parseData(Object) parseData(I)} method
     * parameter. It should be used only when the input data is unknown at compile time.
     *
     * @param object {@code Object} to parse to data type {@code R}.
     * @return the result of parsing given {@code Object} to data type {@code R}.
     */
    public R parseObject(@NotNull Object object) {
        return parse((I) object);
    }
}
