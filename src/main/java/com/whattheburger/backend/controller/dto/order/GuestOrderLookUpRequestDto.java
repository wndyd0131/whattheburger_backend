package com.whattheburger.backend.controller.dto.order;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class GuestOrderLookUpRequestDto {
    @Email
    private String email;
}
