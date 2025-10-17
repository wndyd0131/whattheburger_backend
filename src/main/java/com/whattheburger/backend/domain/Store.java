package com.whattheburger.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Store {
    @Id
    @Column(name = "store_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long overpassId;
    private Long houseNumber;
    private LocalTime closeTime;
    private LocalTime openTime;
    private String branch;
    @Embedded
    private Address address;
    @Embedded
    private Coordinate coordinate;
    private String phoneNum;
    private String website;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    @OneToMany(mappedBy = "store")
    private List<StoreProduct> storeProducts = new ArrayList<>();
}
