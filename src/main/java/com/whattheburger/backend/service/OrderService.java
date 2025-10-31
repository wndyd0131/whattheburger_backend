package com.whattheburger.backend.service;

import com.whattheburger.backend.controller.dto.cart.CustomRuleRequest;
import com.whattheburger.backend.controller.dto.cart.OptionRequest;
import com.whattheburger.backend.controller.dto.cart.OptionTraitRequest;
import com.whattheburger.backend.controller.dto.order.DeliveryOrderFormRequestDto;
import com.whattheburger.backend.controller.dto.order.OrderFormRequestDto;
import com.whattheburger.backend.controller.dto.order.PickupOrderFormRequestDto;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.cart.Cart;
import com.whattheburger.backend.domain.cart.CartList;
import com.whattheburger.backend.domain.enums.OrderStatus;
import com.whattheburger.backend.domain.enums.OrderType;
import com.whattheburger.backend.domain.enums.PaymentMethod;
import com.whattheburger.backend.domain.enums.PaymentStatus;
import com.whattheburger.backend.domain.order.*;
import com.whattheburger.backend.repository.*;
import com.whattheburger.backend.security.UserDetailsImpl;
import com.whattheburger.backend.service.dto.cart.ProcessedCartDto;
import com.whattheburger.backend.service.exception.ExpiredSessionException;
import com.whattheburger.backend.service.exception.OrderNotFoundException;
import com.whattheburger.backend.service.exception.order.ExpiredOrderSessionException;
import com.whattheburger.backend.service.exception.order.OrderSessionNotFoundException;
import com.whattheburger.backend.util.OrderSessionFactory;
import com.whattheburger.backend.util.SessionKey;
import com.whattheburger.backend.util.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final StoreService storeService;
    private final CartService cartService;
    private final OrderStorage orderStorage;
    private final OrderSessionStorage orderSessionStorage;
    private final OrderSessionFactory orderSessionFactory;
    private final OrderFactory orderFactory;

    public OrderSession updateOrderSession(OrderFormRequestDto orderFormRequestDto, UUID sessionId, Authentication authentication, UUID guestId) {
        SessionKey sessionKey = getSessionKey(guestId, authentication);
        OrderSession orderSession = orderSessionStorage.load(sessionKey)
                .orElseThrow(() -> new OrderSessionNotFoundException(sessionKey.key()));
        if (!orderSession.getSessionId().equals(sessionId))
            throw new OrderSessionNotFoundException(sessionId.toString());

        if (orderFormRequestDto instanceof DeliveryOrderFormRequestDto deliveryFormRequest) {
            orderSession.changeAddressInfo(
                    deliveryFormRequest.streetAddr(),
                    deliveryFormRequest.streetAddrDetail(),
                    deliveryFormRequest.zipCode(),
                    deliveryFormRequest.cityState()
            );
            orderSession.changeContactInfo(
                    deliveryFormRequest.firstName(),
                    deliveryFormRequest.lastName(),
                    deliveryFormRequest.email(),
                    deliveryFormRequest.phoneNum()
            );
            orderSessionStorage.save(sessionKey.key(), orderSession);
        } else if (orderFormRequestDto instanceof PickupOrderFormRequestDto pickUpFormRequest) {
            orderSession.changeContactInfo(
                    pickUpFormRequest.firstName(),
                    pickUpFormRequest.lastName(),
                    pickUpFormRequest.email(),
                    pickUpFormRequest.phoneNum()
            );
            orderSession.changeETA(pickUpFormRequest.eta());
            orderSessionStorage.save(sessionKey.key(), orderSession);
        } else {
            throw new IllegalStateException();
        }
        return orderSession;
    }

    public Order saveOrder(Order order) {
        return orderStorage.save(order);
    }

    public Order loadOrderByCheckoutSessionId(UUID guestId, Authentication authentication, String checkoutSessionId) {
        return orderStorage.loadByCheckoutSessionId(checkoutSessionId)
                .orElseThrow(() -> new OrderNotFoundException());
    }

    public OrderSession createOrderSession(Long storeId, UUID guestId, Authentication authentication, OrderType orderType) {
        ProcessedCartDto processedCartDto = cartService.loadCart(storeId, guestId, authentication);

        SessionKey sessionKey = getSessionKey(guestId, authentication);

        OrderSession orderSession = orderSessionStorage.load(sessionKey)
                .map(existingSession -> {
                    orderSessionFactory.overwriteFromCartDto(
                            processedCartDto,
                            orderType,
                            storeId,
                            existingSession
                    );
                    return existingSession;
                })
                .orElseGet(() -> orderSessionFactory.createFromCartDto(processedCartDto, orderType, storeId));

        orderSessionStorage.save(sessionKey.key(), orderSession);
        return orderSession;
    }

    public void cleanUp(UUID orderSessionId) {
        orderSessionStorage.remove(orderSessionId);
    }

    public OrderSession loadOrderSession(Long storeId, UUID sessionId, UUID guestId, Authentication authentication) {
        SessionKey sessionKey = getSessionKey(guestId, authentication);

        OrderSession orderSession = orderSessionStorage.load(sessionKey)
                .orElseThrow(() -> new OrderSessionNotFoundException(sessionId.toString()));
        if (!orderSession.getSessionId().equals(sessionId)) {
            throw new OrderSessionNotFoundException(sessionId.toString());
        }
        if (!orderSession.getStoreId().equals(storeId)) {
            throw new OrderSessionNotFoundException(sessionId.toString(), storeId);
        }

        return orderSession;
    }

    public OrderSession loadOrderSessionBySessionId(UUID sessionId, UUID guestId, Authentication authentication) {
        OrderSession orderSession = orderSessionStorage.load(sessionId)
                .orElseThrow(() -> new OrderSessionNotFoundException(sessionId.toString()));

        return orderSession;
    }

    public Order transferFromOrderSession(UUID orderSessionId) {
        Order order = orderSessionStorage.load(orderSessionId)
                .map(orderSession -> {
                    Long storeId = orderSession.getStoreId();
                    Store store = storeService.findStoreById(storeId);
                    return orderFactory.createFromOrderSession(orderSession, store);
                })
                .orElseThrow(() -> new OrderSessionNotFoundException(orderSessionId.toString()));
        return order;
    }

//    public Order createOrderPreview(UUID guestId, Authentication authentication, OrderType orderType) {
//        ProcessedCartDto processedCartDto = cartService.loadCart(guestId.toString(), authentication);
//        boolean isUser = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
//
//        if (isUser) {
//            Object principal = authentication.getPrincipal();
//            if (principal instanceof UserDetailsImpl userDetails) {
//                User user = userDetails.getUser();
//                Order userOrderPreview = orderStorage.loadPreviewByUserInfoAndOrderNumberAndOrderStatuses(
//                        new UserPreviewKey(user.getId()),
//                                null,
//                                List.of(OrderStatus.PENDING))
//                        .map(order -> orderFactory.overWriteOrder(processedCartDto, orderType, order))
//                        .orElseGet(() -> {
//                            Order orderPreview = orderFactory.createFromCartDto(processedCartDto, orderType);
//                            orderPreview.changeUser(user);
//                            return orderPreview;
//                        });
//                return orderStorage.save(userOrderPreview);
//            }
//            throw new IllegalStateException();
//        } else {
//            Order guestOrderPreview = orderStorage.loadByUserInfo(new GuestPreviewKey(guestId))
//                    .map(order -> orderFactory.overWriteOrder(processedCartDto, orderType, order))
//                    .orElseGet(() -> {
//                        Order orderPreview = orderFactory.createFromCartDto(processedCartDto, orderType);
//                        orderPreview.changeGuestInfo(new GuestInfo(guestId));
//                        return orderPreview;
//                    });
//            return orderStorage.save(guestOrderPreview);
//        }
//    }

//    public Order loadOrder(UUID guestId, Authentication authentication) {
//        boolean isUser = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
//
//        if (isUser) {
//            Object principal = authentication.getPrincipal();
//            if (principal instanceof UserDetailsImpl userDetails) {
//                User user = userDetails.getUser();
//                Order userOrderPreview = orderStorage.loadByUserInfo(new UserPreviewKey(user.getId()))
//                        .orElseThrow(() -> new OrderNotFoundException(user.getId()));
//                return userOrderPreview;
//            }
//            throw new IllegalStateException();
//        } else {
//            if (guestId == null)
//                throw new IllegalArgumentException();
//            Order guestOrderPreview = orderStorage.loadByUserInfo(new GuestPreviewKey(guestId))
//                    .orElseThrow(() -> OrderNotFoundException.forGuest(guestId));
//            return guestOrderPreview;
//        }
//    }

    public Order loadOrder(UUID orderNumber) {
        return orderStorage.loadByOrderNumber(orderNumber)
                .orElseThrow(() -> OrderNotFoundException.forOrder(orderNumber));
    }

//    public Order loadOrder(UUID orderNumber, Authentication authentication, String guestEmail) {
//        boolean isUser = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
//        if (isUser) {
//            Object principal = authentication.getPrincipal();
//            if (principal instanceof UserDetailsImpl userDetails) {
//                User user = userDetails.getUser();
//                return orderStorage.loadByUserInfoAndOrderNumber(new UserPreviewKey(user.getId()), orderNumber)
//                        .orElseThrow(() -> OrderNotFoundException.forOrder(orderNumber));
//            }
//            throw new IllegalStateException();
//        } else {
//            return orderStorage.loadByEmailAndOrderNumber(guestEmail, orderNumber)
//                    .orElseThrow(() -> OrderNotFoundException.forOrder(orderNumber));
//        }
//    }

//    public Order loadOrder(UUID orderNumber, UUID guestId, Authentication authentication) {
//        boolean isUser = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
//
//        if (isUser) {
//            Object principal = authentication.getPrincipal();
//            if (principal instanceof UserDetailsImpl userDetails) {
//                User user = userDetails.getUser();
//                Order order = orderStorage.loadByUserInfoAndOrderNumber(new UserPreviewKey(user.getId()), orderNumber)
//                        .orElseThrow(() -> OrderNotFoundException.forOrder(orderNumber));
//                return order;
//            }
//            throw new IllegalStateException();
//        } else {
//            if (guestId == null)
//                throw new IllegalArgumentException();
//            Order order = orderStorage.loadByUserInfoAndOrderNumber(new GuestPreviewKey(guestId), orderNumber)
//                    .orElseThrow(() -> OrderNotFoundException.forOrder(orderNumber));
//            return order;
//        }
//    }

    private Optional<OrderPreviewOwnerKey> resolveOwner(Authentication authentication, UUID guestId) {
        boolean isUser = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
        if (isUser) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetailsImpl userDetails) {
                User user = userDetails.getUser();
                return Optional.of(new UserPreviewKey(user.getId()));
            }
            return Optional.empty();
        }
        if (guestId != null) {
            return Optional.of(new GuestPreviewKey(guestId));
        }
        return Optional.empty();

    }

    private SessionKey getSessionKey(UUID guestId, Authentication authentication) {
        boolean isUser = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
        log.info("isUser {}", isUser);
        if (isUser) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetailsImpl userDetailsImpl) {
                return new SessionKey(UserType.USER, "user:" + userDetailsImpl.getUsername());
            }
            log.info("Principal {}", principal);
        }
        return new SessionKey(UserType.GUEST, "guest:" + guestId.toString());
    }

    private void initIdSets(List<OrderSessionProduct> sessionProducts, Set<Long> productIds, Set<Long> customRuleIds, Set<Long> productOptionIds, Set<Long> productOptionOptionQuantityIds, Set<Long> productOptionTraitIds) {
        for (OrderSessionProduct sessionProduct : sessionProducts) { // add requested ids to each set to validate
            productIds.add(sessionProduct.getStoreProductId());
            List<OrderSessionCustomRule> sessionCustomRules = sessionProduct.getOrderSessionCustomRules();
            for (OrderSessionCustomRule sessionCustomRule : sessionCustomRules) {
                customRuleIds.add(sessionCustomRule.getCustomRuleId());
                List<OrderSessionOption> sessionOptions = sessionCustomRule.getOrderSessionOptions();
                for (OrderSessionOption sessionOption : sessionOptions) {
                    productOptionIds.add(sessionOption.getProductOptionId());
                    Optional.ofNullable(sessionOption.getQuantityDetail())
                            .ifPresent(quantityDetail -> productOptionOptionQuantityIds.add(quantityDetail.getProductOptionOptionQuantityId()));
                    List<OrderSessionOptionTrait> sessionOptionTraits = sessionOption.getOrderSessionOptionTraits();
                    for (OrderSessionOptionTrait sessionOptionTrait : sessionOptionTraits) {
                        productOptionTraitIds.add(sessionOptionTrait.getProductOptionTraitId());
                    }
                }
            }
        }
    }
}
