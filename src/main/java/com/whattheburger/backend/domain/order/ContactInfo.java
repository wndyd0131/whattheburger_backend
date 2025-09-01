package com.whattheburger.backend.domain.order;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ContactInfo {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNum;

    public void changeFirstName(String firstName) {
        this.firstName = firstName;
    }
    public void changeLastName(String lastName) {
        this.lastName = lastName;
    }
    public void changeEmail(String email) {
        this.email = email;
    }
    public void changePhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }
}
