package io.jjute.plugin.testsuite.file;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Iterator;
import java.util.Map;

class TextEntryTest<E extends Map.Entry<String, TextEntry>> {

    private static final String SINGLE_ENTRY_ID = getTypeId(TextEntry.Map.EntryType.SINGLE);
    private static final String BLOCK_ENTRY_ID = getTypeId(TextEntry.Map.EntryType.BLOCK);

    private final TextEntry.Map map = new TextEntry.Map();

    @Test
    void afterConstructingTextEntryShouldBeUnmodifiable() {

        map.put("sample text");
        //noinspection ConstantConditions
        Assertions.assertThrows(UnsupportedOperationException.class, () ->
                getLastMapEntry().getValue().lines.add("text"));
    }

    @Test
    void whenIllegalEntryParameterShouldThrowException() {

        assertThrowsIllegalArgumentException(() -> map.put(""));
        assertThrowsIllegalArgumentException(() -> map.put("", new String[]{"1"}));
        assertThrowsIllegalArgumentException(() -> map.put("name", new String[]{}));
        assertThrowsIllegalArgumentException(() -> map.put(new String[] {}));
    }

    @Test
    void shouldMaintainNaturalInsertionOrder() {

        int r = new java.util.Random().nextInt(10);
        int i1 = 1; for (; i1 < r + 10; i1++) {
            map.put("entry" + i1);
        }
        String[] mapKeys = map.keySet().toArray(new String[0]);
        TextEntry[] mapValues = map.values().toArray(new TextEntry[0]);
        for (int i2 = 1; i2 <= mapKeys.length; i2++) {
            Assertions.assertEquals(SINGLE_ENTRY_ID + ':' + i2, mapKeys[i2 - 1]);
            Assertions.assertEquals("entry" + i2, mapValues[i2 - 1].lines.get(0));
        }
    }

    @Test
    void whenGettingTextLinesShouldMaintainNaturalOrder() {

        final int size = new java.util.Random().nextInt(10) + 10;
        for (int i = 0; i < size; i++) {
            map.put("entry" + i);
        }
        java.util.List<String> textLines =  map.getTextLines();
        Assertions.assertEquals(size, textLines.size());

        for (int i = 0; i < textLines.size(); i++) {
            Assertions.assertEquals("entry" + i, textLines.get(i));
        }
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void shouldConstructProperTextEntryPrefix() {

        map.put("single line");
        Assertions.assertEquals(SINGLE_ENTRY_ID + ':' + 1, getLastMapEntry().getKey());

        map.put("unique", new String[]{"a"});
        Assertions.assertEquals("unique", getLastMapEntry().getKey());
        /*
         * Prefix identifier should be 2 instead of 3 because this is the
         * second entry placed in the map that requires an identifier
         */
        map.put(new String[] { "bl1", "bl2", "bl3" });
        Assertions.assertEquals(BLOCK_ENTRY_ID + ':' + 2, getLastMapEntry().getKey());
        /*
         * Assert that the unique entry has been overridden
         */
        map.put("unique", new String[]{"b"});
        Assertions.assertEquals("b", map.get("unique").lines.get(0));
    }

    @TestOnly
    private void assertThrowsIllegalArgumentException(Executable executable) {
        Assertions.assertThrows(IllegalArgumentException.class, executable);
    }

    @TestOnly
    private @Nullable E getLastMapEntry() {

        Iterator<E> iterator = getMapIterator(); E entry = null;
        while (iterator.hasNext()) entry = iterator.next();
        return entry;
    }

    @TestOnly
    @SuppressWarnings("unchecked")
    private Iterator<E> getMapIterator() {
        return (Iterator<E>) map.entrySet().iterator();
    }

    @TestOnly
    private static String getTypeId(TextEntry.Map.EntryType type) {
        return String.valueOf(type.ordinal());
    }
}
