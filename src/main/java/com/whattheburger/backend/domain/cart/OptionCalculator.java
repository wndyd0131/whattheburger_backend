package com.whattheburger.backend.domain.cart;


import com.whattheburger.backend.service.dto.cart.calculator.OptionDetail;
import com.whattheburger.backend.service.dto.cart.calculator.TraitDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OptionCalculator implements PriceCalculator<List<OptionDetail>>{
    private final TraitCalculator traitCalculator;

    @Override
    public BigDecimal calculateTotalPrice(List<OptionDetail> optionDetails) {
        BigDecimal totalPrice = optionDetails.stream()
                .map(this::calculatePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalPrice;
    }

    private BigDecimal calculatePrice(OptionDetail option) {
        List<TraitDetail> traitDetails = option.getTraitDetails();
        if (option.getIsSelected()) {
            BigDecimal traitTotalPrice = traitCalculator.calculateTotalPrice(traitDetails);
            BigDecimal optionPrice = option.getPrice();
            log.info("option price: {}", optionPrice);
            if (option.getQuantityDetail() == null) { // if quantity is numeric
                optionPrice = calculateCountable(option, optionPrice);
            } else { // if quantity is not numeric, such as EASY, LARGE...
                optionPrice = calculateUncountable(option, optionPrice);
            }
            return optionPrice.add(traitTotalPrice);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateCountable(OptionDetail detail, BigDecimal optionPrice) {
        Integer requestedQuantity = detail.getQuantity();
        if (detail.getIsDefault()) { // if option is default
            Integer defaultQuantity = detail.getDefaultQuantity();
            if (requestedQuantity > defaultQuantity) { // valid quantity does not include default quantity
                Integer validQuantity = requestedQuantity - defaultQuantity;
                return optionPrice.multiply(BigDecimal.valueOf(validQuantity));
            }
            return BigDecimal.ZERO;
        }
        // if option is not default, calculate as requested
        return optionPrice.multiply(BigDecimal.valueOf(requestedQuantity));
    }

    private BigDecimal calculateUncountable(OptionDetail detail, BigDecimal optionPrice) {
        BigDecimal quantityPrice = detail.getQuantityDetail().getPrice();
        Long requestedId = detail.getQuantityDetail().getRequestedId();
        if (detail.getIsDefault()) {
            Long defaultId = detail.getQuantityDetail().getDefaultId();
            if (requestedId != defaultId) {
                return quantityPrice;
            }
            return BigDecimal.ZERO;
        }
        return optionPrice.add(quantityPrice);
    }
}
