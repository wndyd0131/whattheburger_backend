package com.whattheburger.backend.controller.dto.order;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.Email;

import java.util.UUID;

@JsonTypeName("delivery")
public record DeliveryOrderFormRequestDto(
        UUID orderNumber,
        String firstName,
        String lastName,
        String streetAddr,
        String streetAddrDetail,
        String zipCode,
        String cityState,
        @Email String email,
        String phoneNum
) implements OrderFormRequestDto {}
