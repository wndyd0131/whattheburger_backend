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
    public List<ValidatedCartDto> validate(
            List<Cart> carts,
            Map<Long, Product> productMap,
            Map<Long, CustomRule> customRuleMap,
            Map<Long, ProductOption> productOptionMap,
            Map<Long, ProductOptionTrait> productOptionTraitMap,
            Map<Long, ProductOptionOptionQuantity> quantityMap
    ) {
        List<ValidatedCartDto> cartDtos = new ArrayList<>();

        for (Cart cart : carts) {
            Long productId = cart.getProductId();
            log.info("Cart {}", productId);
            Product product = Optional.ofNullable(productMap.get(productId))
                    .orElseThrow(() -> new ProductNotFoundException(productId));

            ValidatedProduct validatedProduct = new ValidatedProduct(
                    product
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
                                        productOptionOptionQuantity
                                );
                            })
                            .orElse(null);


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

            cartDtos.add(
                    new ValidatedCartDto(
                            validatedProduct,
                            validatedCustomRules,
                            cart.getQuantity()
                    )
            );
        }
        return cartDtos;
    }
}
