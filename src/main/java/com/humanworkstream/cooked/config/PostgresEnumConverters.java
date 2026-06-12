package com.humanworkstream.cooked.config;

import com.humanworkstream.cooked.enumeration.UnitType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converters for PostgreSQL custom enum types.
 * Each converter maps a Java enum to/from its PostgreSQL string representation.
 */
public class PostgresEnumConverters {

    @Converter(autoApply = true)
    public static class UnitTypeConverter implements AttributeConverter<UnitType, String> {
        @Override public String convertToDatabaseColumn(UnitType attr) { return attr == null ? null : attr.getValue(); }
        @Override public UnitType convertToEntityAttribute(String db) { return db == null ? null : UnitType.fromValue(db); }
    }
}