package com.whattheburger.backend.utils;

import com.whattheburger.backend.domain.Address;
import com.whattheburger.backend.domain.Coordinate;
import com.whattheburger.backend.domain.Store;

import java.util.ArrayList;

public class MockStoreFactory {
    public static Store createStore() {
        return Store.builder()
                .id(1L)
                .website("")
                .phoneNum("")
                .owner(null)
                .houseNumber(1L)
                .storeProducts(new ArrayList<>())
                .coordinate(new Coordinate(1D, 1D))
                .branch("")
                .address(new Address("", "", "", ""))
                .closeTime(null)
                .openTime(null)
                .overpassId(1L)
                .build();
    }
}
