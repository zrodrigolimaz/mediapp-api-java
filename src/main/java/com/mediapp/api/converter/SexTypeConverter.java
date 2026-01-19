package com.mediapp.api.converter;

import com.mediapp.api.entity.SexType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class SexTypeConverter implements AttributeConverter<SexType, String> {

    @Override
    public String convertToDatabaseColumn(SexType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public SexType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return SexType.valueOf(dbData);
    }
}

