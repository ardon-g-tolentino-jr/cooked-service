package com.humanworkstream.cooked.converter;

import com.humanworkstream.cooked.enumeration.UserRole;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class UserRoleConverter implements AttributeConverter<UserRole, String> {

    @Override
    public String convertToDatabaseColumn(UserRole value) {
        return value != null ? value.name() : UserRole.USER.name();
    }

    @Override
    public UserRole convertToEntityAttribute(String dbData) {
        return dbData != null ? UserRole.valueOf(dbData.toUpperCase()) : UserRole.USER;
    }
}
