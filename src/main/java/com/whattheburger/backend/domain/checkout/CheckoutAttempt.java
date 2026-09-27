package com.whattheburger.backend.domain.checkout;

import com.whattheburger.backend.domain.enums.DiscountType;
import com.whattheburger.backend.domain.enums.OrderType;
import com.whattheburger.backend.domain.enums.PaymentStatus;
import com.whattheburger.backend.domain.order.AddressInfo;
import com.whattheburger.backend.domain.order.ContactInfo;
import com.whattheburger.backend.domain.order.OrderSessionProduct;
import com.whattheburger.backend.util.OrderRecordJsonConverter;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "checkout_attempt")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class CheckoutAttempt {

    @Id
    @Column(name = "checkout_attempt_id", nullable = false)
    private UUID checkoutAttemptId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;

    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "order_note")
    private String orderNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Embedded
    private ContactInfo contactInfo;

    @Embedded
    private AddressInfo addressInfo;

    @Column(name = "eta")
    private Instant eta;

    @Column(name = "arrived_time")
    private Instant arrivedTime;

    @Column(name = "guest_id", nullable = false)
    private UUID guestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type")
    private DiscountType discountType;

    @Column(name = "order_status_duration")
    private Integer orderStatusDuration;

    @Convert(converter = OrderRecordJsonConverter.class)
    @Column(name = "order_record", nullable = false, columnDefinition = "JSON")
    private List<OrderSessionProduct> orderRecord;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        if (this.checkoutAttemptId == null) {
            this.checkoutAttemptId = UUID.randomUUID();
        }
        if (this.paymentStatus == null) {
            this.paymentStatus = PaymentStatus.PENDING;
        }
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
