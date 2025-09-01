package com.whattheburger.backend.domain.cart;


import com.whattheburger.backend.service.dto.cart.calculator.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OptionCalculator implements PriceCalculator<List<OptionCalculatorDto>, OptionCalculationResult>{

    @Override
    public OptionCalculationResult calculateTotalPrice(List<OptionCalculatorDto> optionCalculatorDtos) {
        List<OptionCalculationDetail> optionCalculationDetails = optionCalculatorDtos.stream()
                .map(optionCalculatorDto -> calculatePrice(optionCalculatorDto))
                .toList();
        BigDecimal optionTotalPrice = optionCalculationDetails.stream()
                .map(OptionCalculationDetail::getCalculatedOptionPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OptionCalculationResult(
                optionCalculationDetails,
                optionTotalPrice
        );
    }

    public OptionCalculationDetail calculatePrice(OptionCalculatorDto optionCalculatorDto) {

        if (optionCalculatorDto.getIsSelected()) {
            TraitCalculationResult traitCalculationResult = optionCalculatorDto.getTraitCalculationResult();
            BigDecimal traitTotalPrice = traitCalculationResult.getTraitTotalPrice();
            BigDecimal optionPrice = optionCalculatorDto.getPrice();
            QuantityCalculationDetail quantityCalculationDetail = null;
            log.info("option price: {}", optionPrice);
            if (optionCalculatorDto.getQuantityCalculatorDto() == null) { // if quantity is numeric
                optionPrice = calculateCountable(optionCalculatorDto, optionPrice);
            } else { // if quantity is not numeric, such as EASY, LARGE...
                BigDecimal quantityPrice = optionCalculatorDto.getQuantityCalculatorDto().getPrice();
                quantityCalculationDetail = calculateUncountable(optionCalculatorDto);
                if (optionCalculatorDto.getIsDefault()) { // if default, only count quantity price
                    optionPrice = quantityCalculationDetail.getCalculatedQuantityPrice();
                }
                else { // if not, option + quantity
                    optionPrice = optionPrice.add(quantityPrice);
                }
            }
            BigDecimal calculatedPrice = optionPrice.add(traitTotalPrice);
            return new OptionCalculationDetail(
                    optionCalculatorDto.getProductOptionId(),
                    calculatedPrice,
                    traitCalculationResult.getTraitCalculationDetails(),
                    quantityCalculationDetail
            );
        } else {
            QuantityCalculationDetail quantityCalculationDetail = null;
            if (optionCalculatorDto.getQuantityCalculatorDto() != null)
                quantityCalculationDetail = new QuantityCalculationDetail(optionCalculatorDto.getQuantityCalculatorDto().getDefaultId(), BigDecimal.ZERO);
            return new OptionCalculationDetail(
                    optionCalculatorDto.getProductOptionId(),
                    BigDecimal.ZERO,
                    optionCalculatorDto.getTraitCalculationResult().getTraitCalculationDetails(),
                    quantityCalculationDetail
            );
        }

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

    private BigDecimal calculateCountable(OptionCalculatorDto detail, BigDecimal optionPrice) {
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

    private QuantityCalculationDetail calculateUncountable(OptionCalculatorDto optionCalculatorDto) {
        BigDecimal quantityPrice = optionCalculatorDto.getQuantityCalculatorDto().getPrice();
        Long requestedId = optionCalculatorDto.getQuantityCalculatorDto().getRequestedId();
        Long defaultId = optionCalculatorDto.getQuantityCalculatorDto().getDefaultId();
        if (requestedId != defaultId) {
            return new QuantityCalculationDetail(
                    requestedId,
                    quantityPrice
            );
        }
        return new QuantityCalculationDetail(
                requestedId,
                BigDecimal.ZERO
        );
    }
}
