package com.whattheburger.backend.controller;

import com.whattheburger.backend.domain.order.Order;
import com.whattheburger.backend.service.InventoryPerfSupport;
import com.whattheburger.backend.service.InventoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Dev/perf-only endpoints for load testing {@link InventoryService#deductStock}.
 *
 * <p>Only active when {@code perf.enabled=true}. Hit {@code /setup} once to seed both fixtures
 * (and reset stock before a run), then hammer {@code /deduct/single} or {@code /deduct/multi}
 * with a load tester.
 */
@RestController
@ConditionalOnProperty(name = "perf.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "PerfTest")
public class PerfTestController {

    private final InventoryPerfSupport inventoryPerfSupport;
    private final InventoryService inventoryService;

    @PostMapping("/api/v1/perf/inventory/setup")
    public ResponseEntity<Map<String, Object>> setup() {
        InventoryPerfSupport.SetupResult setupResult = inventoryPerfSupport.setupFixture();
        return ResponseEntity.ok(inventoryPerfSupport.toResponseMap(setupResult));
    }

    @PostMapping("/api/v1/perf/inventory/deduct/single")
    public ResponseEntity<Void> deductSingle() {
        return deduct(InventoryPerfSupport.Scenario.SINGLE);
    }

    @PostMapping("/api/v1/perf/inventory/deduct/multi")
    public ResponseEntity<Void> deductMulti() {
        return deduct(InventoryPerfSupport.Scenario.MULTI);
    }

    private ResponseEntity<Void> deduct(InventoryPerfSupport.Scenario scenario) {
        Order order = inventoryPerfSupport.buildOrder(scenario);
        inventoryService.deductStock(order);
        return ResponseEntity.ok().build();
    }
}
