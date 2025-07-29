package com.whattheburger.backend.domain.cart;


import com.whattheburger.backend.service.dto.cart.calculator.CalculatedTraitDto;
import com.whattheburger.backend.service.dto.cart.calculator.OptionCalcDetail;
import com.whattheburger.backend.service.dto.cart.calculator.TraitCalcDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OptionCalculator implements PriceCalculator<List<OptionCalcDetail>>{

    @Override
    public BigDecimal calculateTotalPrice(List<OptionCalcDetail> details) {
        BigDecimal totalPrice = details.stream()
                .map(this::calculatePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalPrice;
    }

    public BigDecimal calculatePrice(OptionCalcDetail optionDetail) {
        if (optionDetail.getIsSelected()) {
            BigDecimal traitTotalPrice = optionDetail.getTraitTotalPrice();
            BigDecimal optionPrice = optionDetail.getPrice();
            log.info("option price: {}", optionPrice);
            if (optionDetail.getQuantityCalcDetail() == null) { // if quantity is numeric
                optionPrice = calculateCountable(optionDetail, optionPrice);
            } else { // if quantity is not numeric, such as EASY, LARGE...
                optionPrice = calculateUncountable(optionDetail, optionPrice);
            }
            return optionPrice.add(traitTotalPrice);
        }
        return BigDecimal.ZERO;
    }

//    public BigDecimal calculatePrice(OptionCalcDetail option) {
//        List<TraitCalcDetail> traitCalcDetails = option.getTraitCalcDetails();
//        if (option.getIsSelected()) {
//            BigDecimal traitTotalPrice = traitCalculator.calculateTotalPrice(traitCalcDetails);
//            BigDecimal optionPrice = option.getPrice();
//            log.info("option price: {}", optionPrice);
//            if (option.getQuantityCalcDetail() == null) { // if quantity is numeric
//                optionPrice = calculateCountable(option, optionPrice);
//            } else { // if quantity is not numeric, such as EASY, LARGE...
//                optionPrice = calculateUncountable(option, optionPrice);
//            }
//            return optionPrice.add(traitTotalPrice);
//        }
//        return BigDecimal.ZERO;
//    }

    private BigDecimal calculateCountable(OptionCalcDetail detail, BigDecimal optionPrice) {
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

    private BigDecimal calculateUncountable(OptionCalcDetail detail, BigDecimal optionPrice) {
        BigDecimal quantityPrice = detail.getQuantityCalcDetail().getPrice();
        Long requestedId = detail.getQuantityCalcDetail().getRequestedId();
        if (detail.getIsDefault()) {
            Long defaultId = detail.getQuantityCalcDetail().getDefaultId();
            if (requestedId != defaultId) {
                return quantityPrice;
            }
            return BigDecimal.ZERO;
        }
        return optionPrice.add(quantityPrice);
    }
}
