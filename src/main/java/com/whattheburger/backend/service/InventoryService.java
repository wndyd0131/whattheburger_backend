package com.whattheburger.backend.service;

import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.domain.enums.PaymentStatus;
import com.whattheburger.backend.domain.order.*;
import com.whattheburger.backend.repository.ProductOptionOptionQuantityRepository;
import com.whattheburger.backend.repository.ProductOptionRepository;
import com.whattheburger.backend.repository.StoreInventoryRepository;
import com.whattheburger.backend.service.exception.BadRequestException;
import com.whattheburger.backend.service.exception.POOQuantityNotFoundException;
import com.whattheburger.backend.service.exception.ProductOptionNotFoundException;
import com.whattheburger.backend.service.exception.StoreInventoryNotFoundException;
import com.whattheburger.backend.service.exception.cart.InsufficientOptionStockException;
import com.whattheburger.backend.service.exception.cart.InvalidOptionRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final StoreInventoryRepository storeInventoryRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductOptionOptionQuantityRepository productOptionOptionQuantityRepository;

    public void deductStock(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order must not be null");
        }
        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            throw new BadRequestException("Order payment status must be PAID to deduct stock");
        }

        Long storeId = order.getStore().getId();
        Map<Long, Integer> deductionsByIngredient = collectDeductions(order);

        for (Map.Entry<Long, Integer> entry : deductionsByIngredient.entrySet()) {
            Long ingredientId = entry.getKey();
            int amount = entry.getValue();

            storeInventoryRepository.findByStoreIdAndIngredientId(storeId, ingredientId)
                    .orElseThrow(() -> new StoreInventoryNotFoundException(storeId, ingredientId));

            int updated = storeInventoryRepository.deductStockAtomic(storeId, ingredientId, amount);
            if (updated == 0) {
                StoreInventory storeInventory = storeInventoryRepository
                        .findByStoreIdAndIngredientId(storeId, ingredientId)
                        .orElseThrow(() -> new StoreInventoryNotFoundException(storeId, ingredientId));
                throw new InsufficientOptionStockException(null, ingredientId, amount, storeInventory.getCurrentStock());
            }
        }
    }

    private Map<Long, Integer> collectDeductions(Order order) {
        Set<Long> countableProductOptionIds = new HashSet<>();
        Set<Long> uncountablePooqIds = new HashSet<>();

        for (OrderProduct orderProduct : order.getOrderProducts()) {
            for (OrderCustomRule orderCustomRule : orderProduct.getOrderCustomRules()) {
                for (OrderProductOption orderProductOption : orderCustomRule.getOrderProductOptions()) {
                    CountType countType = orderProductOption.getCountType();
                    if (countType == null || countType == CountType.NONE) {
                        continue;
                    }
                    if (countType == CountType.COUNTABLE) {
                        countableProductOptionIds.add(orderProductOption.getProductOptionId());
                    } else if (countType == CountType.UNCOUNTABLE) {
                        QuantityDetail quantityDetail = orderProductOption.getQuantityDetail();
                        if (quantityDetail == null) {
                            throw InvalidOptionRequestException.missingQuantityDetail(orderProductOption.getProductOptionId());
                        }
                        uncountablePooqIds.add(quantityDetail.getProductOptionOptionQuantityId());
                    }
                }
            }
        }

        Map<Long, ProductOption> productOptionMap = productOptionRepository
                .findAllWithOptionIngredientsByIdIn(countableProductOptionIds)
                .stream()
                .collect(Collectors.toMap(ProductOption::getId, Function.identity()));

        Map<Long, ProductOptionOptionQuantity> pooqMap = productOptionOptionQuantityRepository
                .findAllWithOptionQuantityIngredientsByIdIn(uncountablePooqIds)
                .stream()
                .collect(Collectors.toMap(ProductOptionOptionQuantity::getId, Function.identity()));

        Map<Long, Integer> deductionsByIngredient = new HashMap<>();

        for (OrderProduct orderProduct : order.getOrderProducts()) {
            int productQuantity = orderProduct.getQuantity();
            for (OrderCustomRule orderCustomRule : orderProduct.getOrderCustomRules()) {
                for (OrderProductOption orderProductOption : orderCustomRule.getOrderProductOptions()) {
                    CountType countType = orderProductOption.getCountType();
                    if (countType == null || countType == CountType.NONE) {
                        continue;
                    }

                    if (countType == CountType.COUNTABLE) {
                        Integer optionQuantity = orderProductOption.getQuantity();
                        if (optionQuantity == null || optionQuantity <= 0) {
                            throw InvalidOptionRequestException.missingCountableQuantity(
                                    orderProductOption.getProductOptionId(),
                                    optionQuantity
                            );
                        }
                        ProductOption productOption = Optional
                                .ofNullable(productOptionMap.get(orderProductOption.getProductOptionId()))
                                .orElseThrow(() -> new ProductOptionNotFoundException(orderProductOption.getProductOptionId()));

                        for (OptionIngredient optionIngredient : productOption.getOption().getOptionIngredients()) {
                            int needed = productQuantity * optionQuantity * optionIngredient.getRequiredQuantity();
                            Long ingredientId = optionIngredient.getIngredient().getId();
                            deductionsByIngredient.merge(ingredientId, needed, Integer::sum);
                        }
                    } else if (countType == CountType.UNCOUNTABLE) {
                        QuantityDetail quantityDetail = orderProductOption.getQuantityDetail();
                        if (quantityDetail == null) {
                            throw InvalidOptionRequestException.missingQuantityDetail(orderProductOption.getProductOptionId());
                        }
                        ProductOptionOptionQuantity pooq = Optional
                                .ofNullable(pooqMap.get(quantityDetail.getProductOptionOptionQuantityId()))
                                .orElseThrow(() -> new POOQuantityNotFoundException(quantityDetail.getProductOptionOptionQuantityId()));

                        for (OptionQuantityIngredient oqi : pooq.getOptionQuantity().getOptionQuantityIngredients()) {
                            int needed = productQuantity * oqi.getRequiredQuantity();
                            Long ingredientId = oqi.getIngredient().getId();
                            deductionsByIngredient.merge(ingredientId, needed, Integer::sum);
                        }
                    }
                }
            }
        }

        return deductionsByIngredient;
    }
}
