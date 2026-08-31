package com.whattheburger.backend.service;

import com.whattheburger.backend.domain.ProductOption;
import com.whattheburger.backend.domain.ProductOptionOptionQuantity;
import com.whattheburger.backend.domain.StoreInventory;
import com.whattheburger.backend.domain.enums.PaymentStatus;
import com.whattheburger.backend.domain.inventory.InventoryRequirementCalculator;
import com.whattheburger.backend.domain.inventory.OrderStockRequirementLineMapper;
import com.whattheburger.backend.domain.inventory.StockRequirementLine;
import com.whattheburger.backend.domain.order.Order;
import com.whattheburger.backend.repository.ProductOptionOptionQuantityRepository;
import com.whattheburger.backend.repository.ProductOptionRepository;
import com.whattheburger.backend.repository.StoreInventoryRepository;
import com.whattheburger.backend.service.exception.StoreInventoryNotFoundException;
import com.whattheburger.backend.service.exception.cart.InsufficientOptionStockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final StoreInventoryRepository storeInventoryRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductOptionOptionQuantityRepository productOptionOptionQuantityRepository;
    private final InventoryRequirementCalculator inventoryRequirementCalculator;

    @Transactional
    public void deductStock(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order must not be null");
        }
        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            throw new IllegalArgumentException("Order payment status must be PAID to deduct stock");
        }

        Map<Long, Integer> deductionsByIngredient = collectDeductions(order);
        if (deductionsByIngredient.isEmpty()) {
            return;
        }

        Long storeId = order.getStore().getId();
        List<Long> ingredientIds = deductionsByIngredient.keySet().stream().sorted().toList();

        List<StoreInventory> lockedInventories = storeInventoryRepository
                .findAllByStoreIdAndIngredientIdInForUpdate(storeId, ingredientIds);

        Map<Long, StoreInventory> inventoryByIngredientId = lockedInventories.stream()
                .collect(Collectors.toMap(si -> si.getIngredient().getId(), Function.identity()));

        for (Long ingredientId : ingredientIds) {
            StoreInventory storeInventory = inventoryByIngredientId.get(ingredientId);
            if (storeInventory == null) {
                throw new StoreInventoryNotFoundException(storeId, ingredientId);
            }
            int amount = deductionsByIngredient.get(ingredientId);
            if (storeInventory.getCurrentStock() < amount) {
                throw new InsufficientOptionStockException(
                        null,
                        ingredientId,
                        amount,
                        storeInventory.getCurrentStock()
                );
            }
            storeInventory.deductStock(amount);
        }
    }

    private Map<Long, Integer> collectDeductions(Order order) {
        List<StockRequirementLine> lines = OrderStockRequirementLineMapper.fromOrder(order);

        Map<Long, ProductOption> productOptionMap = productOptionRepository
                .findAllWithOptionIngredientsByIdIn(
                        OrderStockRequirementLineMapper.collectCountableProductOptionIds(lines)
                )
                .stream()
                .collect(Collectors.toMap(ProductOption::getId, Function.identity()));

        Map<Long, ProductOptionOptionQuantity> pooqMap = productOptionOptionQuantityRepository
                .findAllWithOptionQuantityIngredientsByIdIn(
                        OrderStockRequirementLineMapper.collectUncountablePooqIds(lines)
                )
                .stream()
                .collect(Collectors.toMap(ProductOptionOptionQuantity::getId, Function.identity()));

        return inventoryRequirementCalculator.aggregate(lines, productOptionMap, pooqMap);
    }
}
