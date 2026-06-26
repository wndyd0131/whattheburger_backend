package com.whattheburger.backend.repository;

import com.whattheburger.backend.domain.StoreInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreInventoryRepository extends JpaRepository<StoreInventory, Long> {
    List<StoreInventory> findAllByStoreId(Long storeId);

    Optional<StoreInventory> findByStoreIdAndIngredientId(Long storeId, Long ingredientId);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE StoreInventory si
            SET si.currentStock = si.currentStock - :amount
            WHERE si.store.id = :storeId
              AND si.ingredient.id = :ingredientId
              AND si.currentStock >= :amount
            """)
    int deductStockAtomic(
            @Param("storeId") Long storeId,
            @Param("ingredientId") Long ingredientId,
            @Param("amount") int amount
    );
}
