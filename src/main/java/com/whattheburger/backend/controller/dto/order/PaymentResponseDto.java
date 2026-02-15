package com.whattheburger.backend.controller.dto.order;

import com.whattheburger.backend.domain.enums.PaymentMethod;
import com.whattheburger.backend.domain.enums.PaymentStatus;
import com.whattheburger.backend.domain.order.CardInfo;
import lombok.*;

@NoArgsConstructor
@Getter
@AllArgsConstructor
@Builder
public class PaymentResponseDto {
    private CardInfo cardInfo;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
}
