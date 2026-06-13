package com.humanworkstream.cooked.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum MealSlot {
    BREAKFAST("breakfast"),
    LUNCH("lunch"),
    SNACK("snack"),
    DINNER("dinner");

    @JsonValue
    private final String value;

    MealSlot(String value) { this.value = value; }

    @JsonCreator
    public static MealSlot fromValue(String v) {
        for (MealSlot s : values()) if (s.value.equals(v)) return s;
        throw new IllegalArgumentException("Unknown meal slot: " + v);
    }
}
