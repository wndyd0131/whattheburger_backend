package com.whattheburger.backend.service.store_product;

import com.whattheburger.backend.domain.enums.ModifyType;
import org.springframework.stereotype.Component;

@Component
public class HideStrategy implements StoreProductModificationStrategy {

    @Override
    public ModifyType getSupportedType() {
        return ModifyType.HIDE;
    }

    @Override
    public void execute(StoreProductModificationCommand storeProductModificationCommand) {

    }
}
