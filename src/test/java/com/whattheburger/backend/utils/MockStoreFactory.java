package com.whattheburger.backend.utils;

import com.whattheburger.backend.domain.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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

    public static Store createStore(
            Long storeId,
            String website,
            String phoneNum,
            User owner,
            Long houseNumber,
            Coordinate coordinate,
            String branch,
            Address address,
            LocalTime closeTime,
            LocalTime openTime,
            Long overpassId
    ) {
        return Store.builder()
                .id(storeId)
                .website(website)
                .phoneNum(phoneNum)
                .owner(owner)
                .houseNumber(houseNumber)
                .storeProducts(new ArrayList<>())
                .coordinate(coordinate)
                .branch(branch)
                .address(address)
                .closeTime(closeTime)
                .openTime(openTime)
                .overpassId(overpassId)
                .build();
    }
}
