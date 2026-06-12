package com.humanworkstream.cooked.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Difficulty {
    EASY("easy"),
    MEDIUM("medium"),
    HARD("hard");

    @JsonValue
    private final String value;

    Difficulty(String value) { this.value = value; }

    @JsonCreator
    public static Difficulty fromValue(String v) {
        for (Difficulty d : values()) if (d.value.equals(v)) return d;
        throw new IllegalArgumentException("Unknown difficulty: " + v);
    }
}