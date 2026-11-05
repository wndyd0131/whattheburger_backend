package com.whattheburger.backend.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class StoreInventoryBatchRepository {

    private static final int MAX_BATCH_SIZE = 100;

    private static final String DEDUCT_STOCK_ATOMIC_SQL = """
            UPDATE store_inventory
            SET current_stock = current_stock - ?
            WHERE store_id = ?
              AND ingredient_id = ?
              AND current_stock >= ?
            """;

    private final JdbcTemplate jdbcTemplate;

    /**
     * Returns one update count per ingredient, in the same order as {@code ingredientIds}.
     */
    public int[] deductStockAtomicBatch(Long storeId, List<Long> ingredientIds, Map<Long, Integer> amounts) {
        int[][] chunks = jdbcTemplate.batchUpdate(
                DEDUCT_STOCK_ATOMIC_SQL,
                ingredientIds,
                MAX_BATCH_SIZE,
                (ps, ingredientId) -> {
                    int amount = amounts.get(ingredientId);
                    ps.setInt(1, amount);
                    ps.setLong(2, storeId);
                    ps.setLong(3, ingredientId);
                    ps.setInt(4, amount);
                }
        );
        return Arrays.stream(chunks).flatMapToInt(Arrays::stream).toArray();
    }
}
