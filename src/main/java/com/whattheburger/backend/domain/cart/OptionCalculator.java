package com.whattheburger.backend.domain.cart;


import com.whattheburger.backend.service.dto.cart.calculator.OptionDetail;
import com.whattheburger.backend.service.dto.cart.calculator.TraitDetail;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class OptionCalculator implements PriceCalculator<List<OptionDetail>>{
    private final TraitCalculator traitCalculator = new TraitCalculator();

    @Override
    public Double calculateTotalPrice(List<OptionDetail> optionDetails) {
        double totalPrice = optionDetails.stream()
                .mapToDouble(this::calculatePrice)
                .peek(value -> log.info("option value {}", value))
                .sum();
        return totalPrice;
    }

    private Double calculatePrice(OptionDetail option) {
        List<TraitDetail> traitDetails = option.getTraitDetails();
        if (option.getIsSelected()) {
            double traitTotalPrice = traitCalculator.calculateTotalPrice(traitDetails);
            Double optionPrice = option.getPrice();
            log.info("option price: {}", optionPrice);
            if (option.getQuantityDetail() == null) { // if quantity is numeric
                optionPrice = calculateCountable(option, optionPrice);
            } else { // if quantity is not numeric, such as EASY, LARGE...
                optionPrice = calculateUncountable(option, optionPrice);
            }
            return optionPrice + traitTotalPrice;
        }
        else
            return 0D;
    }

    private Double calculateCountable(OptionDetail detail, Double optionPrice) {
        Integer requestedQuantity = detail.getQuantity();
        if (detail.getIsDefault()) { // if option is default
            Integer defaultQuantity = detail.getDefaultQuantity();
            if (requestedQuantity > defaultQuantity) { // valid quantity does not include default quantity
                Integer validQuantity = requestedQuantity - defaultQuantity;
                return optionPrice * validQuantity;
            }
            return 0D;
        }
        else { // if option is not default, calculate as requested
            return optionPrice * requestedQuantity;
        }
    }

    private Double calculateUncountable(OptionDetail detail, Double optionPrice) {
        Double quantityPrice = detail.getQuantityDetail().getPrice();
        Long requestedId = detail.getQuantityDetail().getRequestedId();
        if (detail.getIsDefault()) {
            Long defaultId = detail.getQuantityDetail().getDefaultId();
            if (requestedId != defaultId) {
                return optionPrice + quantityPrice;
            }
        }
        else {
            return optionPrice + quantityPrice;
        }
        return optionPrice + quantityPrice;
    }
}
