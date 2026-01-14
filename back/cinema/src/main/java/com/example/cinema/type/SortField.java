package com.example.cinema.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum SortField{
    VIEW, CREATED, UPDATED;

    @JsonCreator
    public static SortField from(String value) {
        if (value == null) return null;
        return SortField.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    @JsonValue
    public String toValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
