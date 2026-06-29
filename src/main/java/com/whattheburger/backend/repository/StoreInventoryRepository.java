package com.whattheburger.backend.repository;

import com.whattheburger.backend.domain.StoreInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoreInventoryRepository extends JpaRepository<StoreInventory, Long> {
    List<StoreInventory> findAllByStoreId(Long storeId);

    Optional<StoreInventory> findByStoreIdAndIngredientId(Long storeId, Long ingredientId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT si FROM StoreInventory si
            WHERE si.store.id = :storeId
              AND si.ingredient.id IN :ingredientIds
            """)
    List<StoreInventory> findAllByStoreIdAndIngredientIdInForUpdate(
            @Param("storeId") Long storeId,
            @Param("ingredientIds") Collection<Long> ingredientIds
    );
}
