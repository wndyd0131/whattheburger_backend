package com.whattheburger.backend.domain;

import com.whattheburger.backend.domain.order.Order;
import com.whattheburger.backend.security.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class User {
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)

    private String lastName;

    private String phoneNum;
    @Column(nullable = false)

    private String email;
    @Column(nullable = false)

    private String password;

    @Column(nullable = false)
    private String zipcode;
    private Integer point;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user")
    private List<Order> orderList = new ArrayList<>();

    public User(String firstName, String lastName, String phoneNum, String zipcode, String email, String password, Role role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNum = phoneNum;
        this.zipcode = zipcode;
        this.email = email;
        this.password = password;
        this.role = role;
    }
}
