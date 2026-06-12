package com.humanworkstream.cooked.config;

import com.humanworkstream.cooked.enumeration.Difficulty;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converters for PostgreSQL custom enum types.
 * Each converter maps a Java enum to/from its PostgreSQL string representation.
 */
public class PostgresEnumConverters {

    @Converter(autoApply = true)
    public static class DifficultyConverter implements AttributeConverter<Difficulty, String> {
        @Override public String convertToDatabaseColumn(Difficulty attr) { return attr == null ? null : attr.getValue(); }
        @Override public Difficulty convertToEntityAttribute(String db) { return db == null ? null : Difficulty.fromValue(db); }
    }
}