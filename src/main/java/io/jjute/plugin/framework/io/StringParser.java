package io.jjute.plugin.framework.io;

import org.jetbrains.annotations.NotNull;

public class StringParser extends DataParser<String, Object> {

    StringParser() {
        super(String.class, "valueOf", Object.class);
    }

    @Override
    public String parse(@NotNull Object type) {
        return toString(type);
    }

    @Override
    public String toString(Object type) {
        return String.valueOf(type);
    }
}
