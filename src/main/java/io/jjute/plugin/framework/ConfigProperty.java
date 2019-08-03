package io.jjute.plugin.framework;

import io.jjute.plugin.framework.parser.DataParser;
import io.jjute.plugin.framework.parser.DataParsingException;
import org.gradle.api.Project;

public interface ConfigProperty<T extends PluginConfig> {

    /**
     * Read this property from the given {@code Project} for this {@code ConfigProperty}
     * using a {@code DataParser} to parse the found property {@code Object} and
     * update the plugin configuration value associated with this property.
     *
     * @param config {@code PluginConfig} to load this property to.
     * @param project {@code Project} instance to search for property data.
     *
     * @throws DataParsingException if a checked exception is thrown while parsing the found property.
     * @throws IllegalStateException if the {@code Field} object associated with this
     *                               {@code Property} is enforcing Java language access
     *                               control and the underlying field is inaccessible.
     */
    default void loadFromProjectProperties(T config, Project project) {

        Object property = project.findProperty(getName());
        /*
         * Properties passed as console arguments or defined in gradle.properties file
         * will always be String objects. This also makes sure the property is not null.
         */
        if (property instanceof String) {
            /*
             * null parser indicated that the property should be interpreted as a String
             */
            @SuppressWarnings("unchecked") Object result = getDataParser() != null ?
                    getDataParser().parse(property) : String.valueOf(property);
            try {
                getPropertyField().set(config, result);
            }
            catch (IllegalAccessException e) {
                throw new IllegalStateException(String.format("Unable to set field %s," +
                        " access through reflection was denied.", getName()), e);
            }
        }
    }

    /**
     * @param config instance of {@code PluginConfig} to read the value from.
     * @return the value of the given {@code PluginConfig} field associated with this property.
     *
     * @throws IllegalStateException if the {@code Field} object associated with this
     *                               {@code Property} is enforcing Java language access
     *                               control and the underlying field is inaccessible.
     */
    default Object getValue(T config) {

        try {
            return getPropertyField().get(config);
        }
        catch (IllegalAccessException e) {
            throw new IllegalStateException(String.format("Unable to read field %s," +
                    " access through reflection was denied.", getName()), e);
        }
    }

    /**
     * @return a {@code Field} object that reflects the declared field of the class represented by this property.
     * @throws IllegalStateException if the field matching the name of this property was not found.
     */
    default java.lang.reflect.Field getPropertyField() {

        try {
            return getPropertyClass().getDeclaredField(getName());
        }
        catch (NoSuchFieldException e) {
            throw new IllegalStateException(String.format("The specified field " +
                    "\"%s\" was not found declared in %s", getName(), getPropertyClass().getName()), e);
        }
    }

    /**
     * @return the name of this property used to access it through DSL.
     */
    String getName();

    /**
     * @return {@code Class} that represents generic type {@code T}.
     */
    Class<T> getPropertyClass();

    /**
     * @return {@code DataParser} used to parse this property.
     */
    DataParser getDataParser();
}
