package com.whattheburger.backend.domain.order;

import com.whattheburger.backend.domain.Store;
import com.whattheburger.backend.domain.User;
import com.whattheburger.backend.domain.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Order {
    @Id
    @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private UUID orderNumber;
    @Column(precision = 10, scale = 2)
    private BigDecimal totalPrice;
    @Embedded
    private CardInfo cardInfo;
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus; //
    @Enumerated(EnumType.STRING)
    private OrderType orderType;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus; //
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod; //
    private String orderNote;
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;
    @Column(precision = 10, scale = 2)
    private BigDecimal taxAmount;
    @Embedded
    private ContactInfo contactInfo;
    @Embedded
    private AddressInfo addressInfo;
    @Embedded
    private GuestInfo guestInfo;
    @Embedded
    private PickupInfo pickupInfo;
    @Column(unique = true)
    private String checkoutSessionId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProduct> orderProducts = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        if (this.orderNumber == null) {
            this.orderNumber = UUID.randomUUID();
        }
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void changeCheckoutSessionId(String checkoutSessionId) {
        this.checkoutSessionId = checkoutSessionId;
    }
    public void changeUser(User user) {
        this.user = user;
        this.guestInfo = null;
    }

    public void changeCardInfo(String cardBrand, String cardLast4, Long cardExpireMonth, Long cardExpireYear) {
        this.cardInfo = new CardInfo(
                cardBrand,
                cardLast4,
                cardExpireMonth,
                cardExpireYear
        );
    }

    public void changeGuestInfo(GuestInfo guestInfo) {
        this.guestInfo = guestInfo;
        this.user = null;
    }

    public void assignOrderProducts(List<OrderProduct> orderProducts) {
        this.orderProducts = orderProducts;
    }

    public void renewOrder(
            List<OrderProduct> orderProducts,
            OrderType orderType,
            BigDecimal totalPrice
    ) {
        this.orderProducts.clear();
        this.orderProducts.addAll(orderProducts);
        this.totalPrice = totalPrice;
        this.orderType = orderType;
    }

    public void updateOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void changePaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void changePaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void changeContactInfo(
            String firstName,
            String lastName,
            String email,
            String phoneNum
    ) {
        this.contactInfo = new ContactInfo(
                firstName,
                lastName,
                email,
                phoneNum
        );
    }

    public void changeAddressInfo(
            String streetAddr,
            String streetAddrDetail,
            String zipCode,
            String cityState
    ) {
        this.addressInfo = new AddressInfo(
                streetAddr,
                streetAddrDetail,
                zipCode,
                cityState
        );
    }

    public void changeETA(
            LocalDateTime eta
    ) {
        if (pickupInfo != null) {
            pickupInfo.changeETA(eta);
        }
    }
}
