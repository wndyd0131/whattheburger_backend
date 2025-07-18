package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.service.dto.cart.calculator.ProductDetail;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ProductCalculator implements PriceCalculator<List<ProductDetail>> {
    private final OptionCalculator optionCalculator = new OptionCalculator();

    @Override
    public Double calculateTotalPrice(List<ProductDetail> productDetails) {
        double totalPrice = productDetails.stream()
                .mapToDouble(this::calculatePrice)
                .peek(value -> log.info("product value {}", value))
                .sum();
        return totalPrice;
    }

    private Double calculatePrice(ProductDetail product) {
        Double productBasePrice = product.getBasePrice();
        Integer productQuantity = product.getQuantity();
        log.info("Product Price: {}", product.getBasePrice());
        log.info("Product Quantity: {}", product.getQuantity());
        double optionTotalPrice = optionCalculator.calculateTotalPrice(product.getOptionDetails());
        log.info("Option Total Price: {}", optionTotalPrice);
        return (productBasePrice + optionTotalPrice) * productQuantity;
    }
}
