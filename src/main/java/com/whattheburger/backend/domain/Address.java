package com.whattheburger.backend.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class Address {
    private String city;
    private String street;
    private String state;
    private String zipcode;
}
