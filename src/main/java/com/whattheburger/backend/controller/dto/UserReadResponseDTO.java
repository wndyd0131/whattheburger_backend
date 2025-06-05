package com.whattheburger.backend.controller.dto;

import com.whattheburger.backend.security.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserReadResponseDTO {
    private Long userId;
    private String firstName;
    private String lastName;
    private String phoneNum;
    private String email;
    private String zipcode;
    private Role role;
}
