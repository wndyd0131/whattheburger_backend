package com.whattheburger.backend.loadtest;

import com.whattheburger.backend.domain.Store;
import com.whattheburger.backend.domain.inventory.OrderStockRequirementLineMapper;
import com.whattheburger.backend.domain.inventory.StockRequirementLine;
import com.whattheburger.backend.domain.order.Order;
import com.whattheburger.backend.loadtest.dto.LoadTestDeductStockRequest;
import com.whattheburger.backend.service.InventoryService;
import com.whattheburger.backend.service.StoreService;
import com.whattheburger.backend.service.exception.cart.InsufficientOptionStockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loadtest.api.enabled", havingValue = "true")
public class LoadTestInventoryController {

    private final StoreService storeService;
    private final LoadTestOrderAssembler loadTestOrderAssembler;
    private final InventoryService inventoryService;

    @PostMapping("/api/v1/loadtest/store/{storeId}/inventory/deduct")
    public ResponseEntity<?> deductStock(
            @PathVariable Long storeId,
            @RequestBody LoadTestDeductStockRequest request
    ) {
        Store store = storeService.loadStoreById(storeId);
        Order order = loadTestOrderAssembler.assemble(store, request);
        try {
            inventoryService.deductStock(order);
        } catch (InsufficientOptionStockException e) {
            log.warn("loadtest deduct insufficient stock storeId={}", storeId, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", "INSUFFICIENT_STOCK",
                    "message", e.getMessage()
            ));
        }

        List<StockRequirementLine> lines = OrderStockRequirementLineMapper.fromOrder(order);
        return ResponseEntity.ok(Map.of(
                "status", "DEDUCTED",
                "storeId", storeId,
                "stockLines", lines.size()
        ));
    }
}
