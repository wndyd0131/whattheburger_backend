package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.controller.dto.cart.*;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.service.dto.cart.*;
import com.whattheburger.backend.service.dto.cart.ProductDetail;
import com.whattheburger.backend.service.exception.*;
import com.whattheburger.backend.service.exception.StoreInventoryNotFoundException;
import com.whattheburger.backend.service.exception.cart.CartItemLimitExceededException;
import com.whattheburger.backend.service.exception.cart.CartStoreProductNotFoundException;
import com.whattheburger.backend.service.exception.cart.InsufficientOptionStockException;
import com.whattheburger.backend.service.exception.cart.InvalidOptionRequestException;
import com.whattheburger.backend.service.exception.cart.StoreProductNotInStoreException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class CartValidator {
    public static final int MAX_CART_ITEMS = 20;

    public boolean canMergeItemCount(int userItemCount, int guestItemCount) {
        return userItemCount + guestItemCount <= MAX_CART_ITEMS;
    }

    public List<ValidatedCartDto> validate (
            CartList cartList,
            Map<Long, StoreProduct> storeProductMap,
            Map<Long, CustomRule> customRuleMap,
            Map<Long, ProductOption> productOptionMap,
            Map<Long, ProductOptionTrait> productOptionTraitMap,
            Map<Long, ProductOptionOptionQuantity> quantityMap,
            Map<Long, StoreInventory> storeInventoryMap
    ) throws ResourceNotFoundException {
        if (cartList.getCarts().size() > MAX_CART_ITEMS) {
            throw new CartItemLimitExceededException(MAX_CART_ITEMS);
        }

        List<Cart> carts = cartList.getCarts();
        Long storeId = cartList.getStoreId();
        List<ValidatedCartDto> validatedCartDtos = new ArrayList<>();

        for (Cart cart : carts) {
            ValidatedCartDto validatedCartDto = validate(storeId, cart, storeProductMap, customRuleMap, productOptionMap, productOptionTraitMap, quantityMap, storeInventoryMap);

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
            Map<Long, ProductOptionOptionQuantity> quantityMap,
            Map<Long, StoreInventory> storeInventoryMap
    ) {
        Long storeProductId = cart.getStoreProductId();
        log.info("storeProduct Id {}", storeProductId);
        StoreProduct storeProduct = Optional.ofNullable(storeProductMap.get(storeProductId))
                .orElseThrow(() -> new CartStoreProductNotFoundException(storeProductId));
        if (!storeProduct.getStore().getId().equals(storeId)) { // check if the store product belongs to the store
            throw new StoreProductNotInStoreException(storeProduct.getId(), storeId);
        }

        ValidatedProduct validatedProduct = new ValidatedProduct(
                storeProduct,
                cart.getQuantity() // product quantity
        );

        Map<Long, StoreOptionDelta> optionDeltaMap = storeProduct.getStoreOptionDeltas().stream()
                .collect(java.util.stream.Collectors.toMap(
                        delta -> delta.getProductOption().getId(),
                        delta -> delta,
                        (existing, replacement) -> replacement
                ));

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

                if (Boolean.TRUE.equals(optionRequest.getIsSelected())
                        && StoreOptionDelta.resolveExtraPrice(
                                productOption,
                                optionDeltaMap.get(productOptionId)
                        ).isEmpty()) {
                    throw new HiddenOptionSelectedException(productOptionId);
                }

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

                if (Boolean.TRUE.equals(optionRequest.getIsSelected())) {
                    validateOptionStock(storeId, productOption, optionRequest, validatedQuantity, storeInventoryMap);
                }

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

    private void validateOptionStock(
            Long storeId,
            ProductOption productOption,
            OptionRequest optionRequest,
            ValidatedQuantity validatedQuantity,
            Map<Long, StoreInventory> storeInventoryMap
    ) {
        CountType countType = productOption.getCountType();
        if (countType == null || countType == CountType.NONE) {
            return;
        }

        if (countType == CountType.COUNTABLE) {
            Integer optionQuantity = optionRequest.getOptionQuantity();
            if (optionQuantity == null || optionQuantity <= 0) {
                throw InvalidOptionRequestException.missingCountableQuantity(productOption.getId(), optionQuantity);
            }
            List<OptionIngredient> optionIngredients = productOption.getOption().getOptionIngredients();
            for (OptionIngredient optionIngredient : optionIngredients) {
                int needed = optionQuantity * optionIngredient.getRequiredQuantity();
                checkStock(storeId, productOption, optionIngredient.getIngredient().getId(), needed, storeInventoryMap);
            }
            return;
        }

        if (countType == CountType.UNCOUNTABLE) {
            if (validatedQuantity == null || validatedQuantity.getSelectedQuantity() == null) {
                throw InvalidOptionRequestException.missingQuantityDetail(productOption.getId());
            }
            OptionQuantity selectedOptionQuantity = validatedQuantity.getSelectedQuantity().getOptionQuantity();
            List<OptionQuantityIngredient> optionQuantityIngredients = selectedOptionQuantity.getOptionQuantityIngredients();
            for (OptionQuantityIngredient oqi : optionQuantityIngredients) {
                int needed = oqi.getRequiredQuantity();
                checkStock(storeId, productOption, oqi.getIngredient().getId(), needed, storeInventoryMap);
            }
        }
    }

    private void checkStock(
            Long storeId,
            ProductOption productOption,
            Long ingredientId,
            int needed,
            Map<Long, StoreInventory> storeInventoryMap
    ) {
        StoreInventory storeInventory = Optional.ofNullable(storeInventoryMap.get(ingredientId))
                .orElseThrow(() -> new StoreInventoryNotFoundException(storeId, ingredientId));
        int currentStock = storeInventory.getCurrentStock();
        if (needed > currentStock) {
            throw new InsufficientOptionStockException(productOption.getId(), ingredientId, needed, currentStock);
        }
    }
}
