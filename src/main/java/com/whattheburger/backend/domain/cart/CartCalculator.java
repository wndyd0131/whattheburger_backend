package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.domain.enums.OptionTraitType;
import com.whattheburger.backend.service.dto.cart.calculator.CalculatorDto;
import com.whattheburger.backend.service.dto.cart.calculator.ProductDetail;
import com.whattheburger.backend.service.dto.cart.calculator.QuantityDetail;
import com.whattheburger.backend.service.dto.cart.calculator.TraitDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CartCalculator {
    private final ProductCalculator productCalculator = new ProductCalculator();

    public Double calculate(CalculatorDto calculatorDto) {
        List<ProductDetail> productDetails = calculatorDto.getProductDetails();
        log.info("product count: {}", productDetails.size());
        Double totalPrice = productCalculator.calculateTotalPrice(productDetails);
        return totalPrice;
    }
}
