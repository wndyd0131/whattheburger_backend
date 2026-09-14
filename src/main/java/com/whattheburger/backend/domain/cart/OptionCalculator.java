package com.whattheburger.backend.domain.cart;


import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.service.dto.cart.calculator.*;
import com.whattheburger.backend.service.exception.cart.InvalidOptionRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

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

            switch (resolveCountType(optionCalculatorDto)) {
                case COUNTABLE -> optionPrice = calculateCountable(optionCalculatorDto, optionPrice);
                case UNCOUNTABLE -> {
                    BigDecimal quantityPrice = optionCalculatorDto.getQuantityCalculatorDto().getPrice();
                    quantityCalculationDetail = calculateUncountable(optionCalculatorDto);
                    if (optionCalculatorDto.getIsDefault()) {
                        optionPrice = quantityCalculationDetail.getCalculatedQuantityPrice();
                    } else {
                        optionPrice = optionPrice.add(quantityPrice);
                    }
                }
                case NONE -> optionPrice = calculateNone(optionCalculatorDto, optionPrice);
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

    private CountType resolveCountType(OptionCalculatorDto dto) {
        if (dto.getCountType() != null) {
            return dto.getCountType();
        }
        if (dto.getQuantityCalculatorDto() != null) {
            return CountType.UNCOUNTABLE;
        }
        if (dto.getQuantity() != null) {
            return CountType.COUNTABLE;
        }
        return CountType.NONE;
    }

    private BigDecimal calculateCountable(OptionCalculatorDto detail, BigDecimal optionPrice) {
        Integer requestedQuantity = detail.getQuantity();
        if (requestedQuantity == null) {
            throw InvalidOptionRequestException.missingCountableQuantity(
                    detail.getProductOptionId(),
                    null
            );
        }
        if (detail.getIsDefault()) {
            Integer defaultQuantity = detail.getDefaultQuantity();
            if (requestedQuantity > defaultQuantity) {
                Integer validQuantity = requestedQuantity - defaultQuantity;
                return optionPrice.multiply(BigDecimal.valueOf(validQuantity));
            }
            return BigDecimal.ZERO;
        }
        return optionPrice.multiply(BigDecimal.valueOf(requestedQuantity));
    }

    private BigDecimal calculateNone(OptionCalculatorDto detail, BigDecimal optionPrice) {
        if (detail.getIsDefault()) {
            return BigDecimal.ZERO;
        }
        return optionPrice;
    }

    private QuantityCalculationDetail calculateUncountable(OptionCalculatorDto optionCalculatorDto) {
        BigDecimal quantityPrice = optionCalculatorDto.getQuantityCalculatorDto().getPrice();
        Long requestedId = optionCalculatorDto.getQuantityCalculatorDto().getRequestedId();
        Long defaultId = optionCalculatorDto.getQuantityCalculatorDto().getDefaultId();
        if (!requestedId.equals(defaultId)) {
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
