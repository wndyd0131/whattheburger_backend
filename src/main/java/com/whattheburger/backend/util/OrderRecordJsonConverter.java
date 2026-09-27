package com.whattheburger.backend.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whattheburger.backend.domain.order.OrderSessionProduct;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class OrderRecordJsonConverter implements AttributeConverter<List<OrderSessionProduct>, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<OrderSessionProduct>> TYPE_REFERENCE = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(List<OrderSessionProduct> attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize order record", e);
        }
    }

    @Override
    public List<OrderSessionProduct> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(dbData, TYPE_REFERENCE);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to deserialize order record", e);
        }
    }
}
