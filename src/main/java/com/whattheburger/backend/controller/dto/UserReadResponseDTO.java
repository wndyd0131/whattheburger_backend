package com.whattheburger.backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserReadResponseDTO {
    private String firstName;
    private String lastName;
    private String phoneNum;
    private String email;
    private String zipcode;
}
