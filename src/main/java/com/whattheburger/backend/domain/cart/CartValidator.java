package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.controller.dto.cart.*;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.domain.inventory.CartStockRequirementLineMapper;
import com.whattheburger.backend.domain.inventory.InventoryRequirementCalculator;
import com.whattheburger.backend.domain.inventory.StockRequirementLine;
import com.whattheburger.backend.service.dto.cart.*;
import com.whattheburger.backend.service.exception.*;
import com.whattheburger.backend.service.exception.StoreInventoryNotFoundException;
import com.whattheburger.backend.service.exception.cart.CartItemLimitExceededException;
import com.whattheburger.backend.service.exception.cart.CartStoreProductNotFoundException;
import com.whattheburger.backend.service.exception.cart.InsufficientOptionStockException;
import com.whattheburger.backend.service.exception.cart.StoreProductNotInStoreException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class CartValidator {
    public static final int MAX_CART_ITEMS = 20;

    private final InventoryRequirementCalculator inventoryRequirementCalculator;

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
            ValidatedCartDto validatedCartDto = validateCartItem(
                    storeId,
                    cart,
                    storeProductMap,
                    customRuleMap,
                    productOptionMap,
                    productOptionTraitMap,
                    quantityMap
            );

            validatedCartDtos.add(validatedCartDto);
        }

        List<StockRequirementLine> lines = CartStockRequirementLineMapper.fromCartList(cartList, productOptionMap);
        validateCartStock(storeId, lines, productOptionMap, quantityMap, storeInventoryMap);

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
        ValidatedCartDto validatedCartDto = validateCartItem(
                storeId,
                cart,
                storeProductMap,
                customRuleMap,
                productOptionMap,
                productOptionTraitMap,
                quantityMap
        );

        List<StockRequirementLine> lines = CartStockRequirementLineMapper.fromCart(cart, productOptionMap);
        validateCartStock(storeId, lines, productOptionMap, quantityMap, storeInventoryMap);

        return validatedCartDto;
    }

    private ValidatedCartDto validateCartItem(
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

    private void validateCartStock(
            Long storeId,
            List<StockRequirementLine> lines,
            Map<Long, ProductOption> productOptionMap,
            Map<Long, ProductOptionOptionQuantity> quantityMap,
            Map<Long, StoreInventory> storeInventoryMap
    ) {
        if (lines.isEmpty()) {
            return;
        }

        Map<Long, Integer> requiredByIngredient = inventoryRequirementCalculator.aggregate(
                lines,
                productOptionMap,
                quantityMap
        );

        for (Map.Entry<Long, Integer> entry : requiredByIngredient.entrySet()) {
            Long ingredientId = entry.getKey();
            int needed = entry.getValue();
            StoreInventory storeInventory = Optional.ofNullable(storeInventoryMap.get(ingredientId))
                    .orElseThrow(() -> new StoreInventoryNotFoundException(storeId, ingredientId));
            int currentStock = storeInventory.getCurrentStock();
            if (needed > currentStock) {
                throw new InsufficientOptionStockException(null, ingredientId, needed, currentStock);
            }
        }
    }
}
