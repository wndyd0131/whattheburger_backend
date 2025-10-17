package com.whattheburger.backend.service.store_product;

import com.whattheburger.backend.service.dto.OptionModificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StoreProductModificationHandler {
    private final ModificationStrategyResolver strategyResolver;
    public void handle(StoreProductModificationCommand storeProductModificationCommand) {
        if (storeProductModificationCommand instanceof CustomRuleModificationCommand customRuleModificationCommand) {

        } else if (storeProductModificationCommand instanceof OptionModificationCommand optionModificationCommand) {
            StoreProductModificationStrategy strategy = strategyResolver.resolve(optionModificationCommand.modifyType());
            strategy.execute(optionModificationCommand);
        } else if (storeProductModificationCommand instanceof TraitModificationCommand traitModificationCommand) {

        }
    }
}
