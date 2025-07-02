package com.whattheburger.backend.service;

import com.whattheburger.backend.controller.dto.cart.*;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.repository.*;
import com.whattheburger.backend.service.exception.*;
import com.whattheburger.backend.service.exception.cart.InvalidCartIndexException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final ProductOptionTraitRepository productOptionTraitRepository;
    private final ProductOptionOptionQuantityRepository productOptionOptionQuantityRepository;

    public List<CartResponseDto> saveCart(String cartId, Authentication authentication, CartRequestDto cartRequestDto) {

        String sessionKey = cartId;

        boolean isUser = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);

        log.info("isUser {}", isUser);
        if (isUser) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                sessionKey = userDetails.getUsername();
            }
            log.info("Principal {}", principal);
        }
        Cart cart = new Cart(cartRequestDto.getProductId(), cartRequestDto.getQuantity(), cartRequestDto.getCustomRuleRequests());

        CartList cartList = Optional.ofNullable(rt.opsForValue().get("cart:" + sessionKey)).orElse(new CartList(new ArrayList<>()));
        log.info("CartList {}", cartList);
        cartList.getCarts().add(cart);
        rt.opsForValue().set("cart:"+sessionKey, cartList);
        return loadCart(sessionKey, authentication);
    }


    public List<CartResponseDto> loadCart(String cartId, Authentication authentication) {

        String sessionKey = cartId;

        boolean isUser = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
        log.info("isUser {}", isUser);
        if (isUser) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                sessionKey = userDetails.getUsername();
            }
            log.info("Principal {}", principal);
        }

        List<CartResponseDto> cartResponseDtos = new ArrayList<>();
        CartList cartList = Optional.ofNullable(rt.opsForValue().get("cart:" + sessionKey)).orElse(new CartList(new ArrayList<>()));
        List<Cart> carts = cartList.getCarts();
        Set<Long> customRuleIds = new HashSet<>();
        Set<Long> productOptionIds = new HashSet<>();
        Set<Long> productOptionTraitIds = new HashSet<>();
        Set<Long> productOptionOptionQuantityIds = new HashSet<>();

        Set<Long> productIds = carts
                .stream().map(Cart::getProductId).collect(Collectors.toSet());
        Map<Long, Product> productMap = productRepository.findAllById(productIds)
                .stream().collect(Collectors.toMap(Product::getId, Function.identity()));

        log.info("CART_LENGTH {}", carts.size());
        for (Cart cart : carts) {
            List<CustomRuleRequest> customRuleRequests = cart.getCustomRuleRequests();
            for (CustomRuleRequest customRuleRequest : customRuleRequests) {
                customRuleIds.add(customRuleRequest.getCustomRuleId());
                List<OptionRequest> optionRequests = customRuleRequest.getOptionRequests();
                for (OptionRequest optionRequest : optionRequests) {
                    log.info("JAM_ID {}", optionRequest.getQuantityDetailRequest());
                    productOptionIds.add(optionRequest.getProductOptionId());

                    Optional.ofNullable(optionRequest.getQuantityDetailRequest())
                            .ifPresent(quantityDetailRequest -> productOptionOptionQuantityIds.add(quantityDetailRequest.getId()));

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
        Map<Long, ProductOptionOptionQuantity> quantityMap = productOptionOptionQuantityRepository.findAllById(productOptionOptionQuantityIds)
                .stream().collect(Collectors.toMap(ProductOptionOptionQuantity::getId, Function.identity()));


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
                    log.info("POOQ_ID {}", optionRequest.getQuantityDetailRequest());
                    Long productOptionId = optionRequest.getProductOptionId();

                    ProductOption productOption = Optional.ofNullable(productOptionMap.get(productOptionId))
                            .orElseThrow(() -> new ProductOptionNotFoundException(productOptionId));

                    log.info("quantityDetailRequest {}", optionRequest.getQuantityDetailRequest());
                    QuantityDetailResponse quantityDetailResponse = Optional.ofNullable(optionRequest.getQuantityDetailRequest())
                            .map(quantityDetailRequest -> {
                                log.info("POOQID {}", quantityDetailRequest);

                                ProductOptionOptionQuantity productOptionOptionQuantity = Optional.ofNullable(quantityMap.get(quantityDetailRequest.getId()))
                                        .orElseThrow(() -> new POOQuantityNotFoundException(quantityDetailRequest.getId()));
                                return new QuantityDetailResponse(
                                        productOptionOptionQuantity.getId(),
                                        productOptionOptionQuantity.getOptionQuantity().getQuantity().getQuantityType()
                                );
                            })
                            .orElse(null);

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
                                        productOption.getOrderIndex(),
                                        quantityDetailResponse
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
    public List<CartResponseDto> modifyItem(String cartId, int cartIdx, CartModifyRequestDto cartRequestDto, Authentication authentication) {
        String sessionKey = cartId;

        boolean isUser = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);

        log.info("isUser {}", isUser);
        if (isUser) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                sessionKey = userDetails.getUsername();
            }
            log.info("Principal {}", principal);
        }
//        Cart cart = new Cart(cartRequestDto.getProductId(), cartRequestDto.getQuantity(), cartRequestDto.getCustomRuleRequests());
        CartList cartList = Optional.ofNullable(rt.opsForValue().get("cart:" + sessionKey)).orElse(new CartList(new ArrayList<>()));
        log.info("CartList {}", cartList);
        List<Cart> carts = cartList.getCarts();

        if (cartIdx >= 0 && cartIdx < carts.size()) {
            Cart cart = carts.get(cartIdx);
            cart.updateCustomRules(cartRequestDto.getCustomRuleRequests());
            rt.opsForValue().set("cart:"+sessionKey, cartList);
        } else {
            throw new InvalidCartIndexException(cartIdx);
        }
        return loadCart(sessionKey, authentication);
    }

    public List<CartResponseDto> deleteItem(String cartId, int cartIdx, Authentication authentication) {
        String sessionKey = cartId;

        boolean isUser = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
        log.info("isUser {}", isUser);
        if (isUser) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                sessionKey = userDetails.getUsername();
            }
            log.info("Principal {}", principal);
        }

        CartList cartList = Optional.ofNullable(rt.opsForValue().get("cart:" + sessionKey)).orElse(new CartList(new ArrayList<>()));
        List<Cart> carts = cartList.getCarts();
        log.info("CartList {}", cartList);
        if (cartIdx >= 0 && cartIdx < carts.size()) {
            carts.remove(cartIdx);
            rt.opsForValue().set("cart:"+sessionKey, cartList);
        } else {
            throw new InvalidCartIndexException(cartIdx);
        }

        return loadCart(sessionKey, authentication);
    }
}
