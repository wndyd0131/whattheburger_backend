package com.whattheburger.backend.domain.order;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CardInfo {
    private String cardBrand;
    private String cardLast4;
    private Long cardExpireMonth;
    private Long cardExpireYear;
}
