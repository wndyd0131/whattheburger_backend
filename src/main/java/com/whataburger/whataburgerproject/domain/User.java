package com.whataburger.whataburgerproject.domain;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.lang.Nullable;

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
    private int point;

    @OneToMany(mappedBy = "user")
    private List<Order> orderList = new ArrayList<>();

    public User(String firstName, String lastName, String phoneNum, String zipcode, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNum = phoneNum;
        this.zipcode = zipcode;
        this.email = email;
        this.password = password;
    }
}
