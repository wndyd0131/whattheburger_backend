package com.whattheburger.backend.service;

import com.whattheburger.backend.controller.dto.cart.*;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.cart.*;
import com.whattheburger.backend.dto_mapper.CartDtoMapper;
import com.whattheburger.backend.repository.*;
import com.whattheburger.backend.security.UserDetailsImpl;
import com.whattheburger.backend.service.dto.cart.ProcessedCartDto;
import com.whattheburger.backend.service.dto.cart.ProcessedProductDto;
import com.whattheburger.backend.service.dto.cart.calculator.CalculatedCartDto;
import com.whattheburger.backend.service.dto.cart.ValidatedCartDto;
import com.whattheburger.backend.service.dto.cart.calculator.ProductCalculationDetail;
import com.whattheburger.backend.service.exception.StoreNotFoundException;
import com.whattheburger.backend.service.exception.cart.CartNotFoundException;
import com.whattheburger.backend.service.exception.cart.InvalidCartIndexException;
import com.whattheburger.backend.util.SessionKey;
import com.whattheburger.backend.util.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final CartSessionStorage cartSessionStorage;
    private final StoreRepository storeRepository;
    private final StoreProductRepository storeProductRepository;
    private final ProductRepository productRepository;
    private final CustomRuleRepository customRuleRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductOptionTraitRepository productOptionTraitRepository;
    private final ProductOptionOptionQuantityRepository productOptionOptionQuantityRepository;
    private final CartDtoMapper cartDtoMapper;
    private final CartValidator cartValidator;
    private final CartCalculator cartCalculator;

    public ProcessedCartDto saveCart(Long storeId, UUID guestId, Authentication authentication, CartCreateRequestDto cartRequestDto) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException(storeId));
        String sessionKey = getSessionKey(guestId, storeId, authentication);
        log.info("WRITE SESSION KEY {}", sessionKey);

        Cart cart = new Cart(cartRequestDto.getStoreProductId(), cartRequestDto.getQuantity(), cartRequestDto.getCustomRuleRequests());

        CartList cartList = cartSessionStorage.load(sessionKey)
                .orElse(new CartList(storeId, new ArrayList<>()));

        log.info("CartList {}", cartList);
        cartList.getCarts().add(cart);
        cartSessionStorage.save(sessionKey, cartList);
        return loadCart(storeId, guestId, authentication);
    }

    public ProcessedCartDto processCart(CartList cartList) {
        List<Cart> carts = cartList.getCarts();
        Set<Long> storeProductIds = carts.stream()
                .map(Cart::getStoreProductId).collect(Collectors.toSet());
        Set<Long> customRuleIds = new HashSet<>();
        Set<Long> productOptionIds = new HashSet<>();
        Set<Long> productOptionTraitIds = new HashSet<>();
        Set<Long> productOptionOptionQuantityIds = new HashSet<>();

        initIdSets(carts, customRuleIds, productOptionIds, productOptionOptionQuantityIds, productOptionTraitIds);

        Map<Long, StoreProduct> storeProductMap = storeProductRepository.findAllById(storeProductIds)
                .stream().collect(Collectors.toMap(StoreProduct::getId, Function.identity()));
        Map<Long, CustomRule> customRuleMap = customRuleRepository.findAllById(customRuleIds)
                .stream().collect(Collectors.toMap(CustomRule::getId, Function.identity()));
        Map<Long, ProductOption> productOptionMap = productOptionRepository.findAllById(productOptionIds)
                .stream().collect(Collectors.toMap(ProductOption::getId, Function.identity()));
        Map<Long, ProductOptionTrait> productOptionTraitMap = productOptionTraitRepository.findAllById(productOptionTraitIds)
                .stream().collect(Collectors.toMap(ProductOptionTrait::getId, Function.identity()));
        Map<Long, ProductOptionOptionQuantity> quantityMap = productOptionOptionQuantityRepository.findAllById(productOptionOptionQuantityIds)
                .stream().collect(Collectors.toMap(ProductOptionOptionQuantity::getId, Function.identity()));

        List<ValidatedCartDto> validatedCartDtos = cartValidator.validate(
                cartList,
                storeProductMap,
                customRuleMap,
                productOptionMap,
                productOptionTraitMap,
                quantityMap
        );

        CalculatedCartDto calculatedCartDto = cartCalculator.calculateTotalPrice(
                carts,
                storeProductMap,
                customRuleMap,
                productOptionMap,
                productOptionTraitMap,
                quantityMap
        );

        log.info("Cart Total {}", calculatedCartDto.getCartCalculationResult().getCartTotalPrice());

        ProcessedCartDto processedCartDto = cartDtoMapper.toProcessedCartDto(validatedCartDtos, calculatedCartDto);

//        BigDecimal totalPrice = cartCalculator.calculate(calculatorDto);

        return processedCartDto;
    }

    public ProcessedCartDto loadCart(Long storeId, UUID guestId, Authentication authentication) {

        String sessionKey = getSessionKey(guestId, storeId, authentication);
        log.info("LOAD SESSION KEY {}", sessionKey);

        CartList cartList = cartSessionStorage.load(sessionKey)
                .orElse(new CartList(storeId, new ArrayList<>()));

        ProcessedCartDto processedCartDto = processCart(cartList);

        return processedCartDto;
    }

    public ProcessedProductDto loadCartByIdx(Long storeId, UUID guestId, int cartIdx, Authentication authentication) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException(storeId));
        String sessionKey = getSessionKey(guestId, storeId, authentication);

        CartList cartList = cartSessionStorage.load(sessionKey)
                .orElse(new CartList(storeId, new ArrayList<>()));
        log.info("CartList {}", cartList);
        List<Cart> carts = cartList.getCarts();

        Set<Long> storeProductIds = carts
                .stream().map(Cart::getStoreProductId).collect(Collectors.toSet());
        Set<Long> customRuleIds = new HashSet<>();
        Set<Long> productOptionIds = new HashSet<>();
        Set<Long> productOptionTraitIds = new HashSet<>();
        Set<Long> productOptionOptionQuantityIds = new HashSet<>();

        initIdSets(carts, customRuleIds, productOptionIds, productOptionOptionQuantityIds, productOptionTraitIds);

        Map<Long, StoreProduct> storeProductMap = storeProductRepository.findAllById(storeProductIds)
                .stream().collect(Collectors.toMap(StoreProduct::getId, Function.identity()));
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

            ValidatedCartDto validatedCartDto = cartValidator.validate(storeId, cart, storeProductMap, customRuleMap, productOptionMap, productOptionTraitMap, quantityMap);
            ProductCalculationDetail productCalculationDetail = cartCalculator.calculateProductPrice(cart, storeProductMap, customRuleMap, productOptionMap, productOptionTraitMap, quantityMap);

            ProcessedProductDto processedProductDto = cartDtoMapper.toProcessedProductDto(validatedCartDto, productCalculationDetail);

            return processedProductDto;
        }
        throw new InvalidCartIndexException(cartIdx);
    }

    public ProcessedCartDto modifyItem(Long storeId, UUID guestId, int cartIdx, List<CustomRuleRequest> customRuleRequests, Authentication authentication) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException(storeId));
        String sessionKey = getSessionKey(guestId, storeId, authentication);

//        Cart cart = new Cart(cartRequestDto.getProductId(), cartRequestDto.getQuantity(), cartRequestDto.getCustomRuleRequests());
        CartList cartList = cartSessionStorage.load(sessionKey)
                .orElse(new CartList(storeId, new ArrayList<>()));
        log.info("CartList {}", cartList);
        List<Cart> carts = cartList.getCarts();

        if (cartIdx >= 0 && cartIdx < carts.size()) {
            Cart cart = carts.get(cartIdx);
            cart.updateCustomRules(customRuleRequests);
            cartSessionStorage.save(sessionKey, cartList);
        } else {
            throw new InvalidCartIndexException(cartIdx);
        }
        return loadCart(storeId, guestId, authentication);
    }

    public ProcessedCartDto modifyItemQuantity(Long storeId, UUID guestId, int cartIdx, int quantity, Authentication authentication) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException(storeId));
        String sessionKey = getSessionKey(guestId, storeId, authentication);

//        Cart cart = new Cart(cartRequestDto.getProductId(), cartRequestDto.getQuantity(), cartRequestDto.getCustomRuleRequests());
        CartList cartList = cartSessionStorage.load(sessionKey)
                .orElse(new CartList(storeId, new ArrayList<>()));
        log.info("CartList {}", cartList);
        List<Cart> carts = cartList.getCarts();

        if (cartIdx >= 0 && cartIdx < carts.size()) {
            Cart cart = carts.get(cartIdx);
            cart.updateProduct(quantity);
            cartSessionStorage.save(sessionKey, cartList);
        } else {
            throw new InvalidCartIndexException(cartIdx);
        }
        return loadCart(storeId, guestId, authentication);
    }

    @PreAuthorize("hasRole('USER')")
    public ProcessedCartDto mergeCart(Long storeId, UUID guestId, Authentication authentication) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException(storeId));
        String userKey = getUserKey(storeId, authentication);
        String guestKey = getGuestKey(storeId, guestId);
        CartList userCartList = cartSessionStorage.load(userKey)
                .orElse(new CartList(storeId, new ArrayList<>()));
        CartList guestCartList = cartSessionStorage.load(guestKey)
                .orElse(new CartList(storeId, new ArrayList<>()));
        List<Cart> carts = userCartList.getCarts();
        guestCartList.getCarts().stream()
                .forEach(cart -> carts.add(cart));
        guestCartList.clearCartList();
        cartSessionStorage.save(userKey, userCartList);
        cartSessionStorage.save(guestKey, guestCartList);
        return loadCart(storeId, guestId, authentication);
    }

    public ProcessedCartDto deleteItem(Long storeId, UUID guestId, int cartIdx, Authentication authentication) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException(storeId));
        String sessionKey = getSessionKey(guestId, storeId, authentication);

        CartList cartList = cartSessionStorage.load(sessionKey)
                .orElse(new CartList(storeId, new ArrayList<>()));
        List<Cart> carts = cartList.getCarts();
        log.info("CartList {}", cartList);
        if (cartIdx >= 0 && cartIdx < carts.size()) {
            carts.remove(cartIdx);
            cartSessionStorage.save(sessionKey, cartList);
        } else {
            throw new InvalidCartIndexException(cartIdx);
        }

        return loadCart(storeId, guestId, authentication);
    }

    public UUID getSessionId(String sessionKey) {
        log.info("session key log {}", sessionKey);
        CartList cartList = cartSessionStorage.load(sessionKey)
                .orElseThrow(() -> new CartNotFoundException(sessionKey));
        return cartList.getSessionId();
    }

    public void cleanUp(UUID sessionId) {
        cartSessionStorage.remove(sessionId);
    }

    public ProcessedCartDto deleteAllItem(Long storeId, UUID guestId, Authentication authentication) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException(storeId));
        String sessionKey = getSessionKey(guestId, storeId, authentication);

        CartList cartList = cartSessionStorage.load(sessionKey)
                .orElse(new CartList(storeId, new ArrayList<>()));
        cartList.clearCartList();
        log.info("CartList {}", cartList);
        cartSessionStorage.save(sessionKey, cartList);


        return loadCart(storeId, guestId, authentication);
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

    private String getSessionKey(UUID guestId, Long storeId, Authentication authentication) {
        boolean isUser = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
        log.info("isUser {}", isUser);
        if (isUser) {
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            log.info("Principal {}", principal);
            return "cart:store:" + storeId + ":" + principal.getUsername();
        }
        return "cart:store:" + storeId + ":" + guestId;
    }

    private String getUserKey(Long storeId, Authentication authentication) {
        boolean isUser = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
        log.info("isUser {}", isUser);
        if (isUser) {
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            log.info("Principal {}", principal);
            return "cart:store:" + storeId + ":" + principal.getUsername();
        }
        throw new IllegalStateException();
    }

    private String getGuestKey(Long storeId, UUID guestId) {
        return "cart:store:" + storeId + ":" + guestId;
    }

    private Map<Class<?>, Map<Long, ?>> fetchAllRelatedEntitiesToMap(List<Cart> carts) {
        Set<Long> productIds = carts
                .stream().map(Cart::getStoreProductId).collect(Collectors.toSet());
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
