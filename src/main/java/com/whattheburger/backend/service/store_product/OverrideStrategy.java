package com.whattheburger.backend.service.store_product;

import com.whattheburger.backend.domain.ProductOption;
import com.whattheburger.backend.domain.StoreOptionDelta;
import com.whattheburger.backend.domain.enums.DeltaType;
import com.whattheburger.backend.domain.enums.ModifyType;
import com.whattheburger.backend.repository.ProductOptionRepository;
import com.whattheburger.backend.repository.StoreOptionDeltaRepository;
import com.whattheburger.backend.service.exception.ProductOptionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OverrideStrategy implements StoreProductModificationStrategy {
    private final ProductOptionRepository productOptionRepository;
    private final StoreOptionDeltaRepository storeOptionDeltaRepository;

    @Override
    public ModifyType getSupportedType() {
        return ModifyType.OVERRIDE;
    }

    @Override
    public void execute(StoreProductModificationCommand storeProductModificationCommand) {
        if (storeProductModificationCommand instanceof OptionModificationCommand optionModificationCommand) {
            Long productId = optionModificationCommand.productId();
            Long optionId = optionModificationCommand.optionId();
            ProductOption productOption = productOptionRepository.findByProductIdAndOptionId(productId, optionId)
                    .orElseThrow(() -> new ProductOptionNotFoundException(productId, optionId));
            StoreOptionDelta storeOptionDelta = storeOptionDeltaRepository.findById(productOption.getId())
                    .orElseGet(() -> new StoreOptionDelta(
                            productOption,
                            optionModificationCommand.storeProduct()
                    ));
            storeOptionDelta.override(
                    optionModificationCommand.extraPrice(),
                    DeltaType.OVERRIDE
            );
            storeOptionDeltaRepository.save(storeOptionDelta); // can be optimized
        }
    }
}
