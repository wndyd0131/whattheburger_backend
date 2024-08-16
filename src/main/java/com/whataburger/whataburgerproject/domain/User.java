package com.whataburger.whataburgerproject.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class User {
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String phoneNum;
    @Embedded
    private Address address;

    private String email;
    private String password;
    @OneToMany(mappedBy = "user")
    private List<Order> orderList = new ArrayList<>();
}
