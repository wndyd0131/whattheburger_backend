package com.whataburger.whataburgerproject.domain;

import com.whataburger.whataburgerproject.domain.enums.OrderStatus;
import com.whataburger.whataburgerproject.domain.enums.OrderType;
import com.whataburger.whataburgerproject.domain.enums.PaymentMethod;
import com.whataburger.whataburgerproject.domain.enums.PaymentStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long orderPrice;
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;
    @Enumerated(EnumType.STRING)
    private OrderType orderType;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    private String orderNote;
    private double discountApplied;
    private double taxAmount;
    // private Store store;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order")
    private List<OrderProduct> orderProducts = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
