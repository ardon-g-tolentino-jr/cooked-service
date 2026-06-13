package com.humanworkstream.cooked.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRole {
    USER,
    ADMIN;

    @JsonValue
    public String toJson() {
        return name();
    }

    @JsonCreator
    public static UserRole fromValue(String value) {
        if (value == null) return USER;
        for (UserRole r : values()) {
            if (r.name().equalsIgnoreCase(value)) return r;
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }
}
