package com.whattheburger.backend.service;

import com.whattheburger.backend.controller.dto.cart.*;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.repository.CustomRuleRepository;
import com.whattheburger.backend.repository.ProductOptionRepository;
import com.whattheburger.backend.repository.ProductOptionTraitRepository;
import com.whattheburger.backend.repository.ProductRepository;
import com.whattheburger.backend.service.exception.CustomRuleNotFoundException;
import com.whattheburger.backend.service.exception.ProductNotFoundException;
import com.whattheburger.backend.service.exception.ProductOptionNotFoundException;
import com.whattheburger.backend.service.exception.ProductOptionTraitNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final RedisTemplate<String, CartList> rt;
    private final ProductRepository productRepository;
    private final CustomRuleRepository customRuleRepository;
    private final ProductOptionRepository productOptionRepository;
    private  final ProductOptionTraitRepository productOptionTraitRepository;

    public List<CartResponseDto> saveCart(String sessionId, CartRequestDto cartRequestDto) {
        Cart cart = new Cart(cartRequestDto.getProductId(), cartRequestDto.getQuantity(), cartRequestDto.getCustomRuleRequests());
        CartList cartList = Optional.ofNullable(rt.opsForValue().get("cart:" + sessionId)).orElse(new CartList(new ArrayList<>()));
        log.info("CartList {}", cartList);
        cartList.getCarts().add(cart);
        rt.opsForValue().set("cart:"+sessionId, cartList);
        return loadCart(sessionId);
    }

    public List<CartResponseDto> loadCart(String sessionId) {
        List<CartResponseDto> cartResponseDtos = new ArrayList<>();
        CartList cartList = Optional.ofNullable(rt.opsForValue().get("cart:" + sessionId)).orElse(new CartList(new ArrayList<>()));
        List<Cart> carts = cartList.getCarts();
        Set<Long> customRuleIds = new HashSet<>();
        Set<Long> productOptionIds = new HashSet<>();
        Set<Long> productOptionTraitIds = new HashSet<>();

        Set<Long> productIds = carts
                .stream().map(Cart::getProductId).collect(Collectors.toSet());
        Map<Long, Product> productMap = productRepository.findAllById(productIds)
                .stream().collect(Collectors.toMap(Product::getId, Function.identity()));


        for (Cart cart : carts) {
            List<CustomRuleRequest> customRuleRequests = cart.getCustomRuleRequests();
            for (CustomRuleRequest customRuleRequest : customRuleRequests) {
                customRuleIds.add(customRuleRequest.getCustomRuleId());
                List<OptionRequest> optionRequests = customRuleRequest.getOptionRequests();
                for (OptionRequest optionRequest : optionRequests) {
                    productOptionIds.add(optionRequest.getProductOptionId());
                    List<OptionTraitRequest> optionTraitRequests = optionRequest.getOptionTraitRequests();
                    for (OptionTraitRequest optionTraitRequest : optionTraitRequests) {
                        productOptionTraitIds.add(optionTraitRequest.getProductOptionTraitId());
                    }
                }
            }
        }

        Map<Long, CustomRule> customRuleMap = customRuleRepository.findAllById(customRuleIds)
                .stream().collect(Collectors.toMap(CustomRule::getId, Function.identity()));
        Map<Long, ProductOption> productOptionMap = productOptionRepository.findAllById(productOptionIds)
                .stream().collect(Collectors.toMap(ProductOption::getId, Function.identity()));
        Map<Long, ProductOptionTrait> productOptionTraitMap = productOptionTraitRepository.findAllById(productOptionTraitIds)
                .stream().collect(Collectors.toMap(ProductOptionTrait::getId, Function.identity()));

        for (Cart cart : carts) {
            Long productId = cart.getProductId();
            Product product = Optional.ofNullable(productMap.get(productId))
                    .orElseThrow(() -> new ProductNotFoundException(productId));
            List<CustomRuleRequest> customRuleRequests = cart.getCustomRuleRequests();

            ProductResponse productResponse = new ProductResponse(
                    productId,
                    product.getName(),
                    product.getProductType(),
                    product.getPrice(),
                    product.getImageSource()
            );

            List<CustomRuleResponse> customRuleResponses = new ArrayList<>();

            for (CustomRuleRequest customRuleRequest : customRuleRequests) {
                Long customRuleId = customRuleRequest.getCustomRuleId();
                CustomRule customRule = Optional.ofNullable(customRuleMap.get(customRuleId))
                        .orElseThrow(() -> new CustomRuleNotFoundException(customRuleId));
                List<OptionRequest> optionRequests = customRuleRequest.getOptionRequests();
                List<OptionResponse> optionResponses = new ArrayList<>();
                log.info("customRuleName: {}", customRule.getName());
                for (OptionRequest optionRequest : optionRequests) {
                        Long productOptionId = optionRequest.getProductOptionId();
                        ProductOption productOption = Optional.ofNullable(productOptionMap.get(productOptionId))
                                .orElseThrow(() -> new ProductOptionNotFoundException(productOptionId));
                        List<OptionTraitRequest> optionTraitRequests = optionRequest.getOptionTraitRequests();
                        List<OptionTraitResponse> optionTraitResponses = new ArrayList<>();
                        log.info("productOptionName: {}", productOption.getOption().getName());
                        for (OptionTraitRequest optionTraitRequest : optionTraitRequests) {
                            Long productOptionTraitId = optionTraitRequest.getProductOptionTraitId();
                            ProductOptionTrait productOptionTrait = Optional.ofNullable(productOptionTraitMap.get(productOptionTraitId))
                                    .orElseThrow(() -> new ProductOptionTraitNotFoundException(productOptionTraitId));
                            optionTraitResponses.add(
                                    new OptionTraitResponse(
                                            productOptionTraitId,
                                            optionTraitRequest.getCurrentValue(),
                                            productOptionTrait.getOptionTrait().getLabelCode(),
                                            productOptionTrait.getOptionTrait().getName(),
                                            productOptionTrait.getOptionTrait().getOptionTraitType()
                                    )
                            );
                        }
                        optionResponses.add(
                                new OptionResponse(
                                        productOptionId,
                                        optionRequest.getOptionQuantity(),
                                        optionRequest.getIsSelected(),
                                        optionTraitResponses,
                                        productOption.getCountType(),
                                        productOption.getMeasureType(),
                                        productOption.getOption().getName(),
                                        productOption.getOrderIndex()
                                )
                        );
                }
                customRuleResponses.add(
                        new CustomRuleResponse(
                                customRuleId,
                                customRule.getName(),
                                customRule.getOrderIndex(),
                                optionResponses
                        )
                );
            }

            cartResponseDtos.add(
                    new CartResponseDto(
                            productResponse,
                            customRuleResponses,
                            cart.getQuantity()
                    )
            );
        }

        log.info("CART SIZE: {}", cartResponseDtos.size());

        return cartResponseDtos;
    }
}
