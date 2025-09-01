package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.service.dto.cart.calculator.ProductCalculationDetail;
import com.whattheburger.backend.service.dto.cart.calculator.ProductCalculationResult;
import com.whattheburger.backend.service.dto.cart.calculator.ProductCalculatorDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductCalculator implements PriceCalculator<List<ProductCalculatorDto>, ProductCalculationResult> {

    @Override
    public ProductCalculationResult calculateTotalPrice(List<ProductCalculatorDto> productCalculatorDtos) {
        List<ProductCalculationDetail> productCalculationDetails = productCalculatorDtos.stream()
                .map(productCalculatorDto -> calculatePrice(productCalculatorDto))
                .toList();
        BigDecimal productTotalPrice = productCalculationDetails.stream()
                .map(ProductCalculationDetail::getCalculatedTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ProductCalculationResult(
                productCalculationDetails,
                productTotalPrice
        );
    }

    public ProductCalculationDetail calculatePrice(ProductCalculatorDto productCalculatorDto) {
        BigDecimal productBasePrice = productCalculatorDto.getBasePrice();
        Integer productQuantity = productCalculatorDto.getQuantity();
        log.info("Product Price: {}", productCalculatorDto.getBasePrice());
        log.info("Product Quantity: {}", productCalculatorDto.getQuantity());
        BigDecimal customRuleTotalPrice = productCalculatorDto.getCustomRuleCalculationResult().getCustomRuleTotalPrice();
        log.info("Option Total Price: {}", customRuleTotalPrice);
        BigDecimal calculatedPrice = productBasePrice.add(customRuleTotalPrice).multiply(BigDecimal.valueOf(productQuantity));
        return new ProductCalculationDetail(
                productCalculatorDto.getProductId(),
                productCalculatorDto.getCustomRuleCalculationResult().getCustomRuleCalculationDetails(),
                calculatedPrice,
                productCalculatorDto.getCustomRuleCalculationResult().getCustomRuleTotalPrice()
        );
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
}
