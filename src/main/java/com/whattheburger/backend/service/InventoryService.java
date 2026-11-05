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
import com.whattheburger.backend.repository.StoreInventoryBatchRepository;
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
    private final StoreInventoryBatchRepository storeInventoryBatchRepository;
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

        List<Long> ingredientIds = deductionsByIngredient.keySet().stream().sorted().toList();

        int[] updatedCounts = storeInventoryBatchRepository
                .deductStockAtomicBatch(storeId, ingredientIds, deductionsByIngredient);
        if (updatedCounts.length != ingredientIds.size()) {
            throw new IllegalStateException(
                    "Batch update count size mismatch: expected=" + ingredientIds.size()
                            + ", actual=" + updatedCounts.length
            );
        }

        for (int i = 0; i < ingredientIds.size(); i++) {
            int updated = updatedCounts[i];
            if (updated == 1) {
                continue;
            }
            Long ingredientId = ingredientIds.get(i);
            if (updated != 0) {
                // e.g. Statement.SUCCESS_NO_INFO: cannot tell whether the conditional update matched
                throw new IllegalStateException(
                        "Unexpected update count " + updated + " for storeId=" + storeId + ", ingredientId=" + ingredientId
                );
            }
            int amount = deductionsByIngredient.get(ingredientId);
            StoreInventory storeInventory = storeInventoryRepository
                    .findByStoreIdAndIngredientId(storeId, ingredientId)
                    .orElseThrow(() -> new StoreInventoryNotFoundException(storeId, ingredientId));
            throw new InsufficientOptionStockException(null, ingredientId, amount, storeInventory.getCurrentStock());
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
