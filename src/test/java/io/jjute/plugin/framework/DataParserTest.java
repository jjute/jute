package io.jjute.plugin.framework;

import io.jjute.plugin.framework.io.DataParser;
import io.jjute.plugin.framework.io.ObjectParser;
import io.jjute.plugin.framework.io.PrimitiveParser;
import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import javafx.util.Pair;

@SuppressWarnings({"unchecked", "WeakerAccess"})
public class DataParserTest<K extends Pair<Object, String>> {

    @SuppressWarnings("unused")
    public static class MagicString {

        final String value;
        private MagicString(String value) {
            this.value = '*' + value + '*';
        }
        public static MagicString valueOf(String value) {
            return new MagicString(value);
        }
        public MagicString nonStaticParser(String value) {
            return new MagicString(value);
        }
    }

    @Test
    public void shouldParsePrimitiveDataTypesToString() {

        java.util.Map<K, Class<?>> map = new java.util.HashMap<>();

        map.put((K) new Pair<Object, String>(1, "1"), Integer.TYPE);
        map.put((K) new Pair(1f, "1.0"), Float.TYPE);
        map.put((K) new Pair(1d, "1.0"), Double.TYPE);
        map.put((K) new Pair(1L, "1"), Long.TYPE);
        map.put((K) new Pair('1', "1"), Character.TYPE);

        parseDataFromMapWithExpectedResult(map);
    }

    @Test
    public void shouldParsePrimitiveDataTypesToObjects() {

        java.util.Map<PrimitiveParser, Object> primitiveParsers = new java.util.HashMap<>();
        primitiveParsers.put(PrimitiveParser.BOOLEAN, true);
        primitiveParsers.put(PrimitiveParser.BYTE, (byte) 1);
        primitiveParsers.put(PrimitiveParser.CHARACTER, 'c');
        primitiveParsers.put(PrimitiveParser.DOUBLE, 1d);
        primitiveParsers.put(PrimitiveParser.FLOAT, 1f);
        primitiveParsers.put(PrimitiveParser.LONG, 1L);
        primitiveParsers.put(PrimitiveParser.INTEGER, 1);
        primitiveParsers.put(PrimitiveParser.SHORT, (short) 1);

        for (Map.Entry<PrimitiveParser, Object> entry : primitiveParsers.entrySet())
        {
            Object expectedValue = entry.getValue();
            Object parsedValue = entry.getKey().parse(expectedValue);
            Assertions.assertEquals(expectedValue, parsedValue);
        }
    }

    @Test
    public void shouldParseStringsWithStringParser() {

        final String text = "text";
        Assertions.assertEquals(text, ObjectParser.STRING.parse(text));
    }

    @Test
    public void whenNoSuchMethodExistsShouldFailParsingType() {

        java.util.Map<K, Class<?>> map = new java.util.HashMap<>();

        map.put((K) new Pair(1, "1"), Byte.TYPE);
        map.put((K) new Pair(1, "1"), Short.TYPE);

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                parseDataFromMapWithExpectedResult(map));
    }

    @Test
    public void shouldParseObjectDataTypes() {

        DataParser<MagicString, String> parser;
        parser = new ObjectParser<>(MagicString.class, "valueOf", String.class);

        MagicString expected = new MagicString("text");
        MagicString parsed = parser.parse("text");

        Assertions.assertEquals(expected.value, parsed.value);
    }

    @TestOnly
    private void parseDataFromMapWithExpectedResult(Map<K, Class<?>> map) {

        for (Map.Entry<K, Class<?>> entry : map.entrySet())
        {
            Pair pair = entry.getKey();
            Class<?> type = entry.getValue();

            DataParser<String, ?> parser = new ObjectParser<>(String.class, "valueOf", type);
            Assertions.assertEquals(pair.getValue(), parser.parseObject(pair.getKey()));
        }
    }

    @Test
    public void whenUsingNotStaticMethodShouldThrowException() {

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new ObjectParser<>(String.class, "nonStaticParser", MagicString.class));
    }
}
