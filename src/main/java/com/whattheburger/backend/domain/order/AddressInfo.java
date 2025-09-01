package com.whattheburger.backend.domain.order;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AddressInfo {
    private String streetAddr;
    private String streetAddrDetail;
    private String zipCode;
    private String cityState;

    public void changeStreetAddr(String streetAddr) {
        this.streetAddr = streetAddr;
    }
    public void changeStreetAddrDetail(String streetAddrDetail) {
        this.streetAddrDetail = streetAddrDetail;
    }
    public void changeZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
    public void changeCityState(String cityState) {
        this.cityState = cityState;
    }
}
