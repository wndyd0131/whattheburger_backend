package com.whattheburger.backend.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class StoreInventoryJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Deducts stock for multiple ingredients in a single UPDATE using CASE.
     *
     * @param ingredientIds sorted ingredient ids (same order used for CASE/IN binding)
     */
    public int deductByCase(Long storeId, List<Long> ingredientIds, Map<Long, Integer> amountsByIngredientId) {
        if (ingredientIds.isEmpty()) {
            return 0;
        }

        StringBuilder caseClause = new StringBuilder();
        StringBuilder inClause = new StringBuilder();
        List<Object> args = new ArrayList<>();

        for (int i = 0; i < ingredientIds.size(); i++) {
            if (i > 0) {
                inClause.append(", ");
            }
            Long ingredientId = ingredientIds.get(i);
            caseClause.append("WHEN ? THEN ? ");
            inClause.append("?");
            args.add(ingredientId);
            args.add(amountsByIngredientId.get(ingredientId));
        }

        args.add(storeId);
        for (Long ingredientId : ingredientIds) {
            args.add(ingredientId);
        }

        String sql = """
                UPDATE store_inventory
                SET current_stock = current_stock - CASE ingredient_id
                %s
                ELSE 0
                END
                WHERE store_id = ?
                  AND ingredient_id IN (%s)
                """.formatted(caseClause.toString().trim(), inClause);

        return jdbcTemplate.update(sql, args.toArray());
    }
}
