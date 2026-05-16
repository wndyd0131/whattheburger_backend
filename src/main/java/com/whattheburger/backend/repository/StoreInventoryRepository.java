package com.whattheburger.backend.repository;

import com.whattheburger.backend.domain.StoreInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreInventoryRepository extends JpaRepository<StoreInventory, Long> {
    List<StoreInventory> findAllByStoreId(Long storeId);
}
