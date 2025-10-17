package com.whattheburger.backend.service.store_product;

import com.whattheburger.backend.domain.enums.ModifyType;
import com.whattheburger.backend.service.exception.ModificationStrategyNotSupportedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ModificationStrategyResolver {
    private Map<ModifyType, StoreProductModificationStrategy> strategyMap;

    @Autowired
    public ModificationStrategyResolver(List<StoreProductModificationStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        StoreProductModificationStrategy::getSupportedType,
                        Function.identity()));
    }

    public StoreProductModificationStrategy resolve(ModifyType modifyType) {
        return Optional.ofNullable(strategyMap.get(modifyType))
                .orElseThrow(() -> new ModificationStrategyNotSupportedException());
    }
}
