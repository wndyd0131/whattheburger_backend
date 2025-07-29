package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.service.dto.cart.calculator.CalculatedCartDto;
import com.whattheburger.backend.service.dto.cart.calculator.ProductCalcDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductCalculator implements PriceCalculator<List<ProductCalcDetail>> {

    @Override
    public BigDecimal calculateTotalPrice(List<ProductCalcDetail> details) {
        BigDecimal totalPrice = details.stream()
                .map(this::calculatePrice)
                .peek(value -> log.info("product value {}", value))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalPrice;
    }

    public BigDecimal calculatePrice(ProductCalcDetail detail) {
        BigDecimal productBasePrice = detail.getBasePrice();
        Integer productQuantity = detail.getQuantity();
        log.info("Product Price: {}", detail.getBasePrice());
        log.info("Product Quantity: {}", detail.getQuantity());
        BigDecimal customRuleTotalPrice = detail.getCustomRuleTotalPrice();
        log.info("Option Total Price: {}", customRuleTotalPrice);
        return productBasePrice.add(customRuleTotalPrice).multiply(BigDecimal.valueOf(productQuantity));
    }

//    public BigDecimal calculatePrice(ProductCalcDetail product) {
//        BigDecimal productBasePrice = product.getBasePrice();
//        Integer productQuantity = product.getQuantity();
//        log.info("Product Price: {}", product.getBasePrice());
//        log.info("Product Quantity: {}", product.getQuantity());
//        BigDecimal optionTotalPrice = optionCalculator.calculateTotalPrice(product.getOptionCalcDetails());
//        log.info("Option Total Price: {}", optionTotalPrice);
//        return productBasePrice.add(optionTotalPrice).multiply(BigDecimal.valueOf(productQuantity));
//    }

    public BigDecimal calculatePrice(CalculatedCartDto calculatedCartDto) {
        return BigDecimal.ZERO;
    }
}
