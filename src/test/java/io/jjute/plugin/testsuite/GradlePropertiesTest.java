package io.jjute.plugin.testsuite;

import io.jjute.plugin.testsuite.GradleProperty;
import static io.jjute.plugin.testsuite.GradleProperty.Type;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("WeakerAccess")
public class GradlePropertiesTest {

    @Test
    public void shouldAssertThatClassDeclarationsAreEncapsulated() {

        Assertions.assertEquals(0, GradleProperty.class.getConstructors().length);
        Assertions.assertEquals(Type.values().length, GradleProperty.Type.class.getFields().length);
    }

    @Test
    public void shouldReturnValidPropertyValues() {

        for (GradleProperty.Type type : Type.values()) {

            GradleProperty property = type.create("defaultProperty", "01-value");
            Assertions.assertEquals("defaultProperty", property.getName());
            Assertions.assertEquals("01-value", property.getValue());
        }
    }

    @Test
    public void shouldDisplayValidPropertyValues() {

        GradleProperty projectProp = Type.PROJECT.create("defaultProperty", "01-value");

        Assertions.assertEquals("-PdefaultProperty=01-value", projectProp.asCLOption());
        Assertions.assertEquals("defaultProperty=01-value", projectProp.asProperty());

        GradleProperty systemProp = Type.SYSTEM.create("defaultProperty", "01-value");

        Assertions.assertEquals("-DdefaultProperty=01-value", systemProp.asCLOption());
        Assertions.assertEquals("systemProp.defaultProperty=01-value", systemProp.asProperty());
    }
}
