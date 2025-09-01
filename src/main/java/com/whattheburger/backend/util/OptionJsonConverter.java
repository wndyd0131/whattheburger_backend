package com.whattheburger.backend.util;

import com.whattheburger.backend.domain.order.OrderSessionOption;
import jakarta.persistence.AttributeConverter;

public class OptionJsonConverter implements AttributeConverter<OrderSessionOption, String> {
    @Override
    public String convertToDatabaseColumn(OrderSessionOption attribute) {
        return null;
    }

    @Override
    public OrderSessionOption convertToEntityAttribute(String dbData) {
        return null;
    }
}
