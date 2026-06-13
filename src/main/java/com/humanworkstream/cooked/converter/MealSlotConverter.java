package com.humanworkstream.cooked.converter;

import com.humanworkstream.cooked.enumeration.MealSlot;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

// Persists MealSlot as its lowercase value ('breakfast'…) in a TEXT column, so
// the DB value matches the JSON value and derived queries bind plain strings.
@Converter(autoApply = true)
public class MealSlotConverter implements AttributeConverter<MealSlot, String> {

    @Override
    public String convertToDatabaseColumn(MealSlot slot) {
        return slot == null ? null : slot.getValue();
    }

    @Override
    public MealSlot convertToEntityAttribute(String dbValue) {
        return dbValue == null ? null : MealSlot.fromValue(dbValue);
    }
}
