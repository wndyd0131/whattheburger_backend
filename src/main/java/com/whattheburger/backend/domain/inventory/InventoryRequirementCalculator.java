package com.whattheburger.backend.domain.inventory;

import com.whattheburger.backend.domain.OptionIngredient;
import com.whattheburger.backend.domain.OptionQuantityIngredient;
import com.whattheburger.backend.domain.ProductOption;
import com.whattheburger.backend.domain.ProductOptionOptionQuantity;
import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.service.exception.POOQuantityNotFoundException;
import com.whattheburger.backend.service.exception.ProductOptionNotFoundException;
import com.whattheburger.backend.service.exception.cart.InvalidOptionRequestException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class InventoryRequirementCalculator {
    public Map<Long, Integer> aggregate(
            List<StockRequirementLine> lines,
            Map<Long, ProductOption> productOptionMap,
            Map<Long, ProductOptionOptionQuantity> pooqMap
    ) {
        Map<Long, Integer> deductionsByIngredient = new HashMap<>();

        for (StockRequirementLine line : lines) {
            CountType countType = line.countType();
            if (countType == null || countType == CountType.NONE) {
                continue;
            }

            if (countType == CountType.COUNTABLE) {
                Integer optionQuantity = line.optionQuantity();
                if (optionQuantity == null || optionQuantity <= 0) {
                    throw InvalidOptionRequestException.missingCountableQuantity(
                            line.productOptionId(),
                            optionQuantity
                    );
                }
                ProductOption productOption = Optional
                        .ofNullable(productOptionMap.get(line.productOptionId()))
                        .orElseThrow(() -> new ProductOptionNotFoundException(line.productOptionId()));

                for (OptionIngredient optionIngredient : productOption.getOption().getOptionIngredients()) {
                    int needed = line.productQuantity() * optionQuantity * optionIngredient.getRequiredQuantity();
                    Long ingredientId = optionIngredient.getIngredient().getId();
                    deductionsByIngredient.merge(ingredientId, needed, Integer::sum);
                }
            } else if (countType == CountType.UNCOUNTABLE) {
                Long pooqId = line.pooqId();
                if (pooqId == null) {
                    throw InvalidOptionRequestException.missingQuantityDetail(line.productOptionId());
                }
                ProductOptionOptionQuantity pooq = Optional
                        .ofNullable(pooqMap.get(pooqId))
                        .orElseThrow(() -> new POOQuantityNotFoundException(pooqId));

                for (OptionQuantityIngredient oqi : pooq.getOptionQuantity().getOptionQuantityIngredients()) {
                    int needed = line.productQuantity() * oqi.getRequiredQuantity();
                    Long ingredientId = oqi.getIngredient().getId();
                    deductionsByIngredient.merge(ingredientId, needed, Integer::sum);
                }
            }
        }

        return deductionsByIngredient;
    }
}
