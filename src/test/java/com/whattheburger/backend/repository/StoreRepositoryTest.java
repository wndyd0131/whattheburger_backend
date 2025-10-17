package com.whattheburger.backend.repository;

import com.whattheburger.backend.domain.Store;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class StoreRepositoryTest {

    @Autowired
    private StoreRepository storeRepository;

    @Test
    public void findNearBy_returnsStores() {
        List<Store> stores = storeRepository.findNearBy(-97.816416, 30.519497, 5000D);
        Assertions.assertNotEquals(0, stores.size());
    }
}
