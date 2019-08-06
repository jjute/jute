package io.jjute.plugin.framework;

import io.jjute.plugin.framework.parser.DataParser;
import io.jjute.plugin.testsuite.core.ProjectTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

class ConfigPropertyTest extends ProjectTest {

    static class DummyPluginConfig extends PluginConfig {

        String month = "June"; String day = "Monday"; String time = "12:30";
    }

    enum TimeProperty implements ConfigProperty<DummyPluginConfig> {

        MONTH_OF_YEAR("month"),
        DAY_OF_WEEK("day"),
        TIME_OF_DAY("time");

        private final String name;

        TimeProperty(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Class<DummyPluginConfig> getPropertyClass() {
            return DummyPluginConfig.class;
        }

        @Override
        public DataParser getDataParser() {
            return null;
        }
    }

    private DummyPluginConfig config;

    @BeforeEach
    void initializePluginConfig() {
        config = new DummyPluginConfig();
    }

    @Test
    void shouldLoadFromProjectProperties() {

        Map<ConfigProperty<DummyPluginConfig>, String> map = new java.util.LinkedHashMap<>();

        map.put(TimeProperty.MONTH_OF_YEAR, "October");
        map.put(TimeProperty.DAY_OF_WEEK, "Friday");
        map.put(TimeProperty.TIME_OF_DAY, "21:00");
        /*
         * Set project properties for all config properties in map
         */
        for (Map.Entry<ConfigProperty<DummyPluginConfig>, String> entry : map.entrySet())
        {
            String key = entry.getKey().getName();
            String value = entry.getValue();

            project.getConvention().getExtraProperties().set(key, value);
            Assertions.assertTrue(project.hasProperty(key));
            Assertions.assertEquals(value, project.findProperty(key));
        }
        /*
         * Update DummyPluginConfig properties with project property values we just added
         */
        for (ConfigProperty<DummyPluginConfig> property : map.keySet()) {
            property.loadFromProjectProperties(config, project);
        }
        /*
         * Validate that all DummyPluginConfig property values have updated
         */
        Assertions.assertEquals("October", config.month);
        Assertions.assertEquals("Friday", config.day);
        Assertions.assertEquals("21:00", config.time);
    }

    @Test
    void shouldGetValidPropertyFieldsAndValues() {

        Assertions.assertEquals("month", TimeProperty.MONTH_OF_YEAR.getPropertyField().getName());
        Assertions.assertEquals("day", TimeProperty.DAY_OF_WEEK.getPropertyField().getName());
        Assertions.assertEquals("time", TimeProperty.TIME_OF_DAY.getPropertyField().getName());

        Assertions.assertEquals("June", TimeProperty.MONTH_OF_YEAR.getValue(config));
        Assertions.assertEquals("Monday", TimeProperty.DAY_OF_WEEK.getValue(config));
        Assertions.assertEquals("12:30", TimeProperty.TIME_OF_DAY.getValue(config));
    }
}
