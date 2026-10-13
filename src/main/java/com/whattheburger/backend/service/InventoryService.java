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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    public void deductStock(Order order) throws InsufficientOptionStockException {
        if (order == null) {
            throw new IllegalArgumentException("Order must not be null");
        }
        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            throw new IllegalArgumentException("Order payment status must be PAID to deduct stock");
        }

        Long storeId = order.getStore().getId();
        Map<Long, Integer> deductionsByIngredient = collectDeductions(order);
        if (deductionsByIngredient.isEmpty()) {
            return;
        }

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
        List<StockRequirementLine> lines = OrderStockRequirementLineMapper.fromOrder(order);

        Map<Long, ProductOption> productOptionMap = productOptionRepository
                .findAllWithOptionByIdIn(
                        OrderStockRequirementLineMapper.collectCountableProductOptionIds(lines)
                )
                .stream()
                .collect(Collectors.toMap(ProductOption::getId, Function.identity()));

        Map<Long, ProductOptionOptionQuantity> pooqMap = productOptionOptionQuantityRepository
                .findAllWithOptionQuantityByIdIn(
                        OrderStockRequirementLineMapper.collectUncountablePooqIds(lines)
                )
                .stream()
                .collect(Collectors.toMap(ProductOptionOptionQuantity::getId, Function.identity()));

        return inventoryRequirementCalculator.aggregate(lines, productOptionMap, pooqMap);
    }
}
