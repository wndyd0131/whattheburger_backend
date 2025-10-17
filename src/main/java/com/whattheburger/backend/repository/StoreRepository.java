package com.whattheburger.backend.repository;

import com.whattheburger.backend.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    @Query(value = """
        WITH params AS (SELECT ST_SRID(POINT(:lon, :lat), 4326) AS center)
        SELECT *
        FROM store
        WHERE ST_Distance_Sphere((SELECT ST_SRID(POINT(store.longitude, store.latitude), 4326)), (SELECT center from params)) <= :radiusMeter
        ORDER BY ST_Distance_Sphere((SELECT ST_SRID(POINT(store.longitude, store.latitude), 4326)), (SELECT center from params))
        """, nativeQuery = true
    )
    List<Store> findNearBy(
            @Param("lon") Double lon,
            @Param("lat") Double lat,
            @Param("radiusMeter") Double radiusMeter
    );
}
