package com.whattheburger.backend.controller.dto.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.domain.enums.ProductType;
import com.whattheburger.backend.domain.order.QuantityDetail;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class ProductResponseDto {
    private Long id;
    private Long storeProductId;
    private Integer quantity;
//    private String forWhom;
    private String name;
    private BigDecimal totalPrice;
    private BigDecimal extraPrice;
    private BigDecimal basePrice;
    private String imageSource;
    private Double calculatedCalories;
    private ProductType productType;
    @JsonProperty("customRuleResponses")
    List<CustomRuleResponseDto> customRuleResponseDtos;
}
