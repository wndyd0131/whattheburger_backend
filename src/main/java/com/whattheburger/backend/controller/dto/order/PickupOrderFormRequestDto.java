package com.whattheburger.backend.controller.dto.order;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.Email;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonTypeName("pickup")
public record PickupOrderFormRequestDto(
        UUID orderNumber,
        String firstName,
        String lastName,
        LocalDateTime eta,
        @Email String email,
        String phoneNum
) implements OrderFormRequestDto {}
