package com.whattheburger.backend.controller.dto;

import com.whattheburger.backend.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserCreateRequestDto {
    private String firstName;
    private String lastName;
    private String phoneNum;
    private String zipcode;
    private String email;
    private String password;
}
