package com.whattheburger.backend.domain.order;

import com.whattheburger.backend.domain.User;
import com.whattheburger.backend.domain.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Setter
public class Order {
    @Id
    @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(precision = 10, scale = 2)
    private BigDecimal totalPrice;
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;
    @Enumerated(EnumType.STRING)
    private OrderType orderType;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    private String orderNote;
    @Enumerated
    private DiscountType discountType;
    @Column(precision = 10, scale = 2)
    private BigDecimal taxAmount;
    @Embedded
    private GuestInfo guestInfo;
    // private Store store;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProduct> orderProducts = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiredAt;

    public void changeUser(User user) {
        this.user = user;
        this.guestInfo = null;
    }

    public void changeGuestInfo(GuestInfo guestInfo) {
        this.guestInfo = guestInfo;
        this.user = null;
    }

    public void assignOrderProducts(List<OrderProduct> orderProducts) {
        this.orderProducts.clear();
        this.orderProducts.addAll(orderProducts);
    }

}
