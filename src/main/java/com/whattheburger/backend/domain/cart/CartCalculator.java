package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.domain.enums.OptionTraitType;
import com.whattheburger.backend.service.dto.cart.calculator.CalculatorDto;
import com.whattheburger.backend.service.dto.cart.calculator.ProductDetail;
import com.whattheburger.backend.service.dto.cart.calculator.QuantityDetail;
import com.whattheburger.backend.service.dto.cart.calculator.TraitDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CartCalculator {
    public Double calculate(CalculatorDto calculatorDto) {
        List<ProductDetail> productDetails = calculatorDto.getProductDetails();
        double totalPrice = productDetails.stream()
                .mapToDouble(productDetail -> calculate(productDetail))
                .peek(value -> log.info("product value {}", value))
                .sum();
        return totalPrice;
    }

    private Double calculate(ProductDetail productDetail) {
        Double productBasePrice = productDetail.getBasePrice();
        Integer productQuantity = productDetail.getQuantity();
        double optionTotalPrice = productDetail.getOptionDetails().stream()
                .mapToDouble(optionDetail -> {
                    double traitTotalPrice = optionDetail.getTraitDetails().stream()
                            .filter(traitDetail -> !(traitDetail.getOptionTraitType() == OptionTraitType.BINARY && traitDetail.getValue() == 0))
                            .mapToDouble(traitDetail -> traitDetail.getPrice())
                            .peek(value -> log.info("trait value {}", value))
                            .sum();
                    Double optionPrice = optionDetail.getPrice();
                    if (optionDetail.getQuantityDetail() == null) {
                        Integer quantity = optionDetail.getQuantity();
                        return optionPrice * quantity + traitTotalPrice;
                    } else {
                        Double quantityPrice = optionDetail.getQuantityDetail().getPrice();
                        return optionPrice + quantityPrice + traitTotalPrice;
                    }
                })
                .peek(value -> log.info("option value {}", value))
                .sum();
        return (productBasePrice + optionTotalPrice) * productQuantity;
    }
}
