package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.service.dto.cart.calculator.ProductDetail;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
public class ProductCalculator implements PriceCalculator<List<ProductDetail>> {
    private final OptionCalculator optionCalculator = new OptionCalculator();

    @Override
    public BigDecimal calculateTotalPrice(List<ProductDetail> productDetails) {
        BigDecimal totalPrice = productDetails.stream()
                .map(this::calculatePrice)
                .peek(value -> log.info("product value {}", value))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalPrice;
    }

    private BigDecimal calculatePrice(ProductDetail product) {
        BigDecimal productBasePrice = product.getBasePrice();
        Integer productQuantity = product.getQuantity();
        log.info("Product Price: {}", product.getBasePrice());
        log.info("Product Quantity: {}", product.getQuantity());
        BigDecimal optionTotalPrice = optionCalculator.calculateTotalPrice(product.getOptionDetails());
        log.info("Option Total Price: {}", optionTotalPrice);
        return productBasePrice.add(optionTotalPrice).multiply(BigDecimal.valueOf(productQuantity));
    }
}
