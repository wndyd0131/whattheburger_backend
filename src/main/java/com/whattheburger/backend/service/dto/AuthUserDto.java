package com.whattheburger.backend.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AuthUserDto {
    private String firstName;
    private String lastName;
    private String phoneNum;
    private String email;
    private String zipcode;
}
