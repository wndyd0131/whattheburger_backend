package com.whattheburger.backend.domain.order;

import com.whattheburger.backend.domain.User;
import com.whattheburger.backend.domain.enums.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class OrderSession {
    private BigDecimal totalPrice;
    private OrderStatus orderStatus;
    private OrderType orderType;
    private PaymentStatus paymentStatus;
    private BigDecimal taxAmount;
    private String orderNote;
    private ContactInfo contactInfo;
    private AddressInfo addressInfo;
    private GuestInfo guestInfo;
    private PickupInfo pickupInfo;
    private DiscountType discountType;
    private List<OrderSessionProduct> orderSessionProducts = new ArrayList<>();

    public void assignOrderSessionProducts(List<OrderSessionProduct> orderSessionProducts) {
        this.orderSessionProducts = orderSessionProducts;
//        this.orderSessionProducts.clear();
//        this.orderProducts.addAll(orderProducts);
    }

    public void renewOrderSession(
            List<OrderSessionProduct> orderSessionProducts,
            OrderType orderType,
            BigDecimal totalPrice
    ) {
        this.orderSessionProducts = orderSessionProducts;
        this.totalPrice = totalPrice;
        this.orderType = orderType;
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
        // PICK UP

}
