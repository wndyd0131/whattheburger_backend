package com.whattheburger.backend.service;

import com.whattheburger.backend.controller.dto.cart.*;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.cart.Cart;
import com.whattheburger.backend.domain.cart.CartCalculator;
import com.whattheburger.backend.domain.cart.CartList;
import com.whattheburger.backend.domain.cart.CartValidator;
import com.whattheburger.backend.dto_mapper.CartDtoMapper;
import com.whattheburger.backend.repository.*;
import com.whattheburger.backend.service.dto.cart.ProcessedCartDto;
import com.whattheburger.backend.service.dto.cart.ProcessedProductDto;
import com.whattheburger.backend.service.dto.cart.calculator.CalculatedCartDto;
import com.whattheburger.backend.service.dto.cart.ValidatedCartDto;
import com.whattheburger.backend.service.dto.cart.calculator.ProductCalcDetail;
import com.whattheburger.backend.service.exception.ResourceNotFoundException;
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
    private final CartDtoMapper cartDtoMapper;
    private final CartValidator cartValidator;
    private final CartCalculator cartCalculator;

    public ProcessedCartDto saveCart(String guestId, Authentication authentication, CartRequestDto cartRequestDto) {

        String sessionKey = getSessionKey(guestId, authentication);

        Cart cart = new Cart(cartRequestDto.getProductId(), cartRequestDto.getQuantity(), cartRequestDto.getCustomRuleRequests());

        CartList cartList = Optional.ofNullable(rt.opsForValue().get("cart:" + sessionKey)).orElse(new CartList(new ArrayList<>()));
        log.info("CartList {}", cartList);
        cartList.getCarts().add(cart);
        rt.opsForValue().set("cart:"+sessionKey, cartList);
        return loadCart(sessionKey, authentication);
    }

    public ProcessedCartDto processCart(List<Cart> carts) {
        Set<Long> productIds = carts
                .stream().map(Cart::getProductId).collect(Collectors.toSet());
        Set<Long> customRuleIds = new HashSet<>();
        Set<Long> productOptionIds = new HashSet<>();
        Set<Long> productOptionTraitIds = new HashSet<>();
        Set<Long> productOptionOptionQuantityIds = new HashSet<>();

        initIdSets(carts, customRuleIds, productOptionIds, productOptionOptionQuantityIds, productOptionTraitIds);

        Map<Long, Product> productMap = productRepository.findAllById(productIds)
                .stream().collect(Collectors.toMap(Product::getId, Function.identity()));
        Map<Long, CustomRule> customRuleMap = customRuleRepository.findAllById(customRuleIds)
                .stream().collect(Collectors.toMap(CustomRule::getId, Function.identity()));
        Map<Long, ProductOption> productOptionMap = productOptionRepository.findAllById(productOptionIds)
                .stream().collect(Collectors.toMap(ProductOption::getId, Function.identity()));
        Map<Long, ProductOptionTrait> productOptionTraitMap = productOptionTraitRepository.findAllById(productOptionTraitIds)
                .stream().collect(Collectors.toMap(ProductOptionTrait::getId, Function.identity()));
        Map<Long, ProductOptionOptionQuantity> quantityMap = productOptionOptionQuantityRepository.findAllById(productOptionOptionQuantityIds)
                .stream().collect(Collectors.toMap(ProductOptionOptionQuantity::getId, Function.identity()));

        List<ValidatedCartDto> validatedCartDtos = cartValidator.validate(
                carts,
                productMap,
                customRuleMap,
                productOptionMap,
                productOptionTraitMap,
                quantityMap
        );

        CalculatedCartDto calculatedCartDto = cartCalculator.calculate(
                carts,
                productMap,
                customRuleMap,
                productOptionMap,
                productOptionTraitMap,
                quantityMap
        );

        ProcessedCartDto processedCartDto = cartDtoMapper.toProcessedCartDto(validatedCartDtos, calculatedCartDto);

//        BigDecimal totalPrice = cartCalculator.calculate(calculatorDto);

        return processedCartDto;
    }

    public ProcessedCartDto loadCart(String guestId, Authentication authentication) {

        String sessionKey = getSessionKey(guestId, authentication);

        CartList cartList = Optional.ofNullable(rt.opsForValue().get("cart:" + sessionKey)).orElse(new CartList(new ArrayList<>()));

        List<Cart> carts = cartList.getCarts();

        ProcessedCartDto processedCartDto = processCart(carts);

        return processedCartDto;
    }

    public ProcessedProductDto loadCartByIdx(String guestId, int cartIdx, Authentication authentication) {
        String sessionKey = getSessionKey(guestId, authentication);

        CartList cartList = Optional.ofNullable(rt.opsForValue().get("cart:" + sessionKey)).orElse(new CartList(new ArrayList<>()));
        log.info("CartList {}", cartList);
        List<Cart> carts = cartList.getCarts();

        Set<Long> productIds = carts
                .stream().map(Cart::getProductId).collect(Collectors.toSet());
        Set<Long> customRuleIds = new HashSet<>();
        Set<Long> productOptionIds = new HashSet<>();
        Set<Long> productOptionTraitIds = new HashSet<>();
        Set<Long> productOptionOptionQuantityIds = new HashSet<>();

        initIdSets(carts, customRuleIds, productOptionIds, productOptionOptionQuantityIds, productOptionTraitIds);

        Map<Long, Product> productMap = productRepository.findAllById(productIds)
                .stream().collect(Collectors.toMap(Product::getId, Function.identity()));
        Map<Long, CustomRule> customRuleMap = customRuleRepository.findAllById(customRuleIds)
                .stream().collect(Collectors.toMap(CustomRule::getId, Function.identity()));
        Map<Long, ProductOption> productOptionMap = productOptionRepository.findAllById(productOptionIds)
                .stream().collect(Collectors.toMap(ProductOption::getId, Function.identity()));
        Map<Long, ProductOptionTrait> productOptionTraitMap = productOptionTraitRepository.findAllById(productOptionTraitIds)
                .stream().collect(Collectors.toMap(ProductOptionTrait::getId, Function.identity()));
        Map<Long, ProductOptionOptionQuantity> quantityMap = productOptionOptionQuantityRepository.findAllById(productOptionOptionQuantityIds)
                .stream().collect(Collectors.toMap(ProductOptionOptionQuantity::getId, Function.identity()));


        if (cartIdx >= 0 && cartIdx < carts.size()) {
            Cart cart = carts.get(cartIdx);

            ValidatedCartDto validatedCartDto = cartValidator.validate(cart, productMap, customRuleMap, productOptionMap, productOptionTraitMap, quantityMap);
            ProductCalcDetail productCalcDetail = cartCalculator.calculate(cart, productMap, customRuleMap, productOptionMap, productOptionTraitMap, quantityMap);

            ProcessedProductDto processedProductDto = cartDtoMapper.toProcessedProductDto(validatedCartDto, productCalcDetail);

            return processedProductDto;
        }
        throw new InvalidCartIndexException(cartIdx);
    }

    public ProcessedCartDto modifyItem(String guestId, int cartIdx, CartModifyRequestDto cartRequestDto, Authentication authentication) {

        String sessionKey = getSessionKey(guestId, authentication);

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

    public ProcessedCartDto deleteItem(String guestId, int cartIdx, Authentication authentication) {

        String sessionKey = getSessionKey(guestId, authentication);

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

    private void initIdSets(List<Cart> carts, Set<Long> customRuleIds, Set<Long> productOptionIds, Set<Long> productOptionOptionQuantityIds, Set<Long> productOptionTraitIds) {
        for (Cart cart : carts) { // add requested ids to each set to validate
            List<CustomRuleRequest> customRuleRequests = cart.getCustomRuleRequests();
            for (CustomRuleRequest customRuleRequest : customRuleRequests) {
                customRuleIds.add(customRuleRequest.getCustomRuleId());
                List<OptionRequest> optionRequests = customRuleRequest.getOptionRequests();
                for (OptionRequest optionRequest : optionRequests) {
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
    }

    private String getSessionKey(String guestId, Authentication authentication) {
        boolean isUser = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
        log.info("isUser {}", isUser);
        if (isUser) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                return userDetails.getUsername();
            }
            log.info("Principal {}", principal);
        }
        return guestId;
    }

    private Map<Class<?>, Map<Long, ?>> fetchAllRelatedEntitiesToMap(List<Cart> carts) {
        Set<Long> productIds = carts
                .stream().map(Cart::getProductId).collect(Collectors.toSet());
        Set<Long> customRuleIds = new HashSet<>();
        Set<Long> productOptionIds = new HashSet<>();
        Set<Long> productOptionTraitIds = new HashSet<>();
        Set<Long> productOptionOptionQuantityIds = new HashSet<>();

        initIdSets(carts, customRuleIds, productOptionIds, productOptionOptionQuantityIds, productOptionTraitIds);

        Map<Long, Product> productMap = productRepository.findAllById(productIds)
                .stream().collect(Collectors.toMap(Product::getId, Function.identity()));
        Map<Long, CustomRule> customRuleMap = customRuleRepository.findAllById(customRuleIds)
                .stream().collect(Collectors.toMap(CustomRule::getId, Function.identity()));
        Map<Long, ProductOption> productOptionMap = productOptionRepository.findAllById(productOptionIds)
                .stream().collect(Collectors.toMap(ProductOption::getId, Function.identity()));
        Map<Long, ProductOptionTrait> productOptionTraitMap = productOptionTraitRepository.findAllById(productOptionTraitIds)
                .stream().collect(Collectors.toMap(ProductOptionTrait::getId, Function.identity()));
        Map<Long, ProductOptionOptionQuantity> quantityMap = productOptionOptionQuantityRepository.findAllById(productOptionOptionQuantityIds)
                .stream().collect(Collectors.toMap(ProductOptionOptionQuantity::getId, Function.identity()));

        Map<Class<?>, Map<Long, ?>> entityMap = new HashMap<>();

        entityMap.put(Product.class, productMap);
        entityMap.put(CustomRule.class, customRuleMap);
        entityMap.put(ProductOption.class, productOptionMap);
        entityMap.put(ProductOptionTrait.class, productOptionTraitMap);
        entityMap.put(ProductOptionOptionQuantity.class, quantityMap);

        return entityMap;
    }
}
