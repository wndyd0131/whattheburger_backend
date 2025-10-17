package com.whattheburger.backend.service.store_product;

import com.whattheburger.backend.domain.enums.ModifyType;
import com.whattheburger.backend.domain.enums.OptionTraitType;
import com.whattheburger.backend.service.dto.cart.calculator.TraitCalculatorDto;

import java.math.BigDecimal;

public interface StoreProductModificationStrategy {
    ModifyType getSupportedType();
    void execute(StoreProductModificationCommand storeProductModificationCommand);
}
