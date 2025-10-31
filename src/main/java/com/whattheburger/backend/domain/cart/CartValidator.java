package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.controller.dto.cart.*;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.service.dto.cart.*;
import com.whattheburger.backend.service.dto.cart.ProductDetail;
import com.whattheburger.backend.service.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class CartValidator {
    public List<ValidatedCartDto> validate (
            CartList cartList,
            Map<Long, StoreProduct> storeProductMap,
            Map<Long, CustomRule> customRuleMap,
            Map<Long, ProductOption> productOptionMap,
            Map<Long, ProductOptionTrait> productOptionTraitMap,
            Map<Long, ProductOptionOptionQuantity> quantityMap
    ) throws ResourceNotFoundException {
        List<Cart> carts = cartList.getCarts();
        Long storeId = cartList.getStoreId();
        List<ValidatedCartDto> validatedCartDtos = new ArrayList<>();

        for (Cart cart : carts) {
            ValidatedCartDto validatedCartDto = validate(storeId, cart, storeProductMap, customRuleMap, productOptionMap, productOptionTraitMap, quantityMap);

            validatedCartDtos.add(validatedCartDto);
        }
        return validatedCartDtos;
    }

    public ValidatedCartDto validate(
            Long storeId,
            Cart cart,
            Map<Long, StoreProduct> storeProductMap,
            Map<Long, CustomRule> customRuleMap,
            Map<Long, ProductOption> productOptionMap,
            Map<Long, ProductOptionTrait> productOptionTraitMap,
            Map<Long, ProductOptionOptionQuantity> quantityMap
    ) {
        Long storeProductId = cart.getStoreProductId();
        log.info("storeProduct Id {}", storeProductId);
        StoreProduct storeProduct = Optional.ofNullable(storeProductMap.get(storeProductId))
                .orElseThrow(() -> new StoreProductNotFoundException(storeProductId));
        if (!storeProduct.getStore().getId().equals(storeId))
            throw new IllegalArgumentException(
                    "StoreProduct " + storeProduct.getId() + " does not belong to store " + storeId
            );

        ValidatedProduct validatedProduct = new ValidatedProduct(
                storeProduct,
                cart.getQuantity() // product quantity
        );

        List<CustomRuleRequest> customRuleRequests = cart.getCustomRuleRequests();
        List<ValidatedCustomRule> validatedCustomRules = new ArrayList<>();

        for (CustomRuleRequest customRuleRequest : customRuleRequests) {
            Long customRuleId = customRuleRequest.getCustomRuleId();
            CustomRule customRule = Optional.ofNullable(customRuleMap.get(customRuleId))
                    .orElseThrow(() -> new CustomRuleNotFoundException(customRuleId));
            List<OptionRequest> optionRequests = customRuleRequest.getOptionRequests();
            List<ValidatedOption> validatedOptions = new ArrayList<>();
            log.info("customRuleName: {}", customRule.getName());
            for (OptionRequest optionRequest : optionRequests) {
                log.info("POOQ_ID {}", optionRequest.getQuantityDetailRequest());
                Long productOptionId = optionRequest.getProductOptionId();

                ProductOption productOption = Optional.ofNullable(productOptionMap.get(productOptionId))
                        .orElseThrow(() -> new ProductOptionNotFoundException(productOptionId));

                log.info("quantityDetailRequest {}", optionRequest.getQuantityDetailRequest());

                ValidatedQuantity validatedQuantity = Optional.ofNullable(optionRequest.getQuantityDetailRequest())
                        .map(quantityDetailRequest -> {
                            log.info("POOQID {}", quantityDetailRequest);


                            ProductOptionOptionQuantity productOptionOptionQuantity = Optional.ofNullable(quantityMap.get(quantityDetailRequest.getId()))
                                    .orElseThrow(() -> new POOQuantityNotFoundException(quantityDetailRequest.getId()));
                            return new ValidatedQuantity(
                                    productOption.getProductOptionOptionQuantities(),
                                    productOptionOptionQuantity
                            );
                        }).orElse(null);


                List<OptionTraitRequest> optionTraitRequests = optionRequest.getOptionTraitRequests();
                List<ValidatedTrait> validatedTraits = new ArrayList<>();

                log.info("productOptionName: {}", productOption.getOption().getName());
                for (OptionTraitRequest optionTraitRequest : optionTraitRequests) {
                    Long productOptionTraitId = optionTraitRequest.getProductOptionTraitId();
                    ProductOptionTrait productOptionTrait = Optional.ofNullable(productOptionTraitMap.get(productOptionTraitId))
                            .orElseThrow(() -> new ProductOptionTraitNotFoundException(productOptionTraitId));
                    validatedTraits.add(
                            new ValidatedTrait(
                                    productOptionTrait,
                                    optionTraitRequest.getCurrentValue()
                            )
                    );
                }
                validatedOptions.add(
                        new ValidatedOption(
                                productOption,
                                validatedTraits,
                                optionRequest.getIsSelected(),
                                optionRequest.getOptionQuantity(),
                                validatedQuantity
                        )
                );
            }
            validatedCustomRules.add(
                    new ValidatedCustomRule(
                            customRule,
                            validatedOptions
                    )
            );
        }

        return new ValidatedCartDto(
                validatedProduct,
                validatedCustomRules
        );
    }
}
