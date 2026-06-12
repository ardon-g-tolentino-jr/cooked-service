package com.humanworkstream.cooked.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum UnitType {
    G("g"),
    KG("kg"),
    ML("ml"),
    L("L"),
    PCS("pcs");

    @JsonValue
    private final String value;

    UnitType(String value) { this.value = value; }

    @JsonCreator
    public static UnitType fromValue(String v) {
        for (UnitType u : values()) if (u.value.equals(v)) return u;
        throw new IllegalArgumentException("Unknown unit: " + v);
    }
}