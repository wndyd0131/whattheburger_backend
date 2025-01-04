package com.whataburger.whataburgerproject.controller.dto;

import com.whataburger.whataburgerproject.domain.Address;
import com.whataburger.whataburgerproject.domain.Product;
import com.whataburger.whataburgerproject.domain.User;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserDto {
    private String firstName;
    private String lastName;
    private String phoneNum;
    private String zipcode;
    private String email;
    private String password;

    private String password2;

    public User toEntity() {
        return new User(
                firstName,
                lastName,
                phoneNum,
                zipcode,
                email,
                password
        );
    }
}
