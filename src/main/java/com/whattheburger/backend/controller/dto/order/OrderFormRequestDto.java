package com.whattheburger.backend.controller.dto.order;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME, // refer type by name
        include = JsonTypeInfo.As.PROPERTY, // find name inside json property
        property = "type" // the property indicates the subtype
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DeliveryOrderFormRequestDto.class, name = "delivery"),
        @JsonSubTypes.Type(value = PickupOrderFormRequestDto.class, name = "pickup")
})
public interface OrderFormRequestDto {
    UUID orderNumber();
}
