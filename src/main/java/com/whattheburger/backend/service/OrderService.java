package com.whattheburger.backend.service;

import com.whattheburger.backend.controller.dto.cart.CustomRuleRequest;
import com.whattheburger.backend.controller.dto.cart.OptionRequest;
import com.whattheburger.backend.controller.dto.cart.OptionTraitRequest;
import com.whattheburger.backend.controller.dto.order.DeliveryOrderFormRequestDto;
import com.whattheburger.backend.controller.dto.order.OrderFormRequestDto;
import com.whattheburger.backend.controller.dto.order.PickupOrderFormRequestDto;
import com.whattheburger.backend.controller.enums.OrderSortType;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private final UserService userService;
    private final OrderStorage orderStorage;
    private final OrderRepository orderRepository;
    private final OrderSessionStorage orderSessionStorage;
    private final OrderSessionFactory orderSessionFactory;
    private final OrderFactory orderFactory;

    public Order loadOrderByOrderId(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    public OrderSession updateOrderSession(OrderFormRequestDto orderFormRequestDto, UUID orderSessionId, Authentication authentication, UUID guestId) {
        OrderSession orderSession = orderSessionStorage.load(orderSessionId)
                .orElseThrow(() -> new OrderSessionNotFoundException(orderSessionId.toString()));

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
            orderSessionStorage.save(orderSession);
        } else if (orderFormRequestDto instanceof PickupOrderFormRequestDto pickUpFormRequest) {
            orderSession.changeContactInfo(
                    pickUpFormRequest.firstName(),
                    pickUpFormRequest.lastName(),
                    pickUpFormRequest.email(),
                    pickUpFormRequest.phoneNum()
            );
            orderSession.changeETA(pickUpFormRequest.eta());
            orderSessionStorage.save(orderSession);
        } else {
            throw new IllegalStateException();
        }
        return orderSession;
    }

    public Order saveOrder(Order order) {
        return orderStorage.save(order);
    }

    public void addOrderToOrderSession(Order order, OrderSession orderSession) {
        orderSession.updateOrderId(order.getId());
        orderSessionStorage.save(orderSession);
    }

    public Order loadOrderByCheckoutSessionId(UUID guestId, Authentication authentication, String checkoutSessionId) {
        return orderStorage.loadByCheckoutSessionId(checkoutSessionId)
                .orElseThrow(() -> new OrderNotFoundException());
    }

    public OrderSession createOrderSession(Long storeId, UUID guestId, UUID orderSessionId, Authentication authentication, OrderType orderType) {
        ProcessedCartDto processedCartDto = cartService.loadCart(storeId, guestId, authentication);

        // add security
        OrderSession orderSession = orderSessionStorage.load(orderSessionId)
                .map(existingSession -> {
                    orderSessionFactory.overwriteFromCartDto(
                            processedCartDto,
                            orderType,
                            storeId,
                            existingSession
                    );
                    log.info("session exists");
                    return existingSession;
                })
                .orElseGet(() -> {
                    log.info("session does not exist");
                    Long userId = null;
                    log.info("Authenticated {}", authentication.isAuthenticated());
                    if (authentication.isAuthenticated()) {
                        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
                        userId = principal.getUserId();
                    }
                    log.info("order session user id: {}", userId);
                    return orderSessionFactory.createFromCartDto(processedCartDto, userId, orderType, storeId);
                });
        log.info("current session key {}", orderSession.getSessionId());
        orderSessionStorage.save(orderSession);
        return orderSession;
    }

    public void updateOrderStatus(Order order, OrderStatus orderStatus) {
        order.updateOrderStatus(orderStatus);
        orderRepository.save(order);
    }

    public void updateOrderSessionPaymentStatus(OrderSession orderSession, PaymentStatus paymentStatus) {
        orderSession.updatePaymentStatus(paymentStatus);
        orderSessionStorage.save(orderSession);
    }

    public void cleanUp(UUID orderSessionId) {
        orderSessionStorage.remove(orderSessionId);
    }

    public OrderSession loadOrderSession(Long storeId, UUID orderSessionId, UUID guestId, Authentication authentication) {
//        SessionKey sessionKey = getSessionKey(guestId, authentication);

        OrderSession orderSession = orderSessionStorage.load(orderSessionId)
                .orElseThrow(() -> new OrderSessionNotFoundException(orderSessionId.toString()));

        if (!orderSession.getStoreId().equals(storeId)) {
            throw new OrderSessionNotFoundException(orderSessionId.toString(), storeId);
        }

        return orderSession;
    }

    public OrderSession loadOrderSessionBySessionId(UUID sessionId, UUID guestId, Authentication authentication) {
        OrderSession orderSession = orderSessionStorage.load(sessionId)
                .orElseThrow(() -> new OrderSessionNotFoundException(sessionId.toString()));

        return orderSession;
    }

    public OrderSession loadOrderSessionByOrderSessionId(UUID orderSessionId) {
        return orderSessionStorage.load(orderSessionId)
                .orElseThrow(() -> new OrderSessionNotFoundException(orderSessionId.toString()));
    }

    public OrderSession loadOrderSessionByCheckoutSessionId(String checkoutSessionId) {
        log.info("requested checkout session id: {}", checkoutSessionId);
        return orderSessionStorage.load("checkout_session:" + checkoutSessionId)
                .orElseThrow(() -> new OrderSessionNotFoundException(checkoutSessionId));
    }

    public Order transferFromOrderSession(OrderSession orderSession) {
        Long storeId = orderSession.getStoreId();
        Long userId = orderSession.getUserId();
        log.info("orderSession.userId: {}", userId);
        User user = userService.loadUserById(userId);
        Store store = storeService.loadStoreById(storeId);
        Order newOrder = orderFactory.createFromOrderSession(orderSession, user, store);
        log.info("orderSession.getOrderId: {}", orderSession.getOrderId());
        orderSessionStorage.save(orderSession);
        return newOrder;
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

    public Order loadOrder(UUID orderNumber, Authentication authentication) {
        if (authentication.isAuthenticated()) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getUserId();
            return orderStorage.loadByOrderNumber(orderNumber, userId)
                    .orElseThrow(() -> OrderNotFoundException.forOrder(orderNumber));
        }
        throw new IllegalStateException();
    }

    public void updateOrderSessionOrderStatus(OrderSession orderSession, OrderStatus orderStatus, Long startTime, Integer duration) {
        orderSession.updateOrderStatus(orderStatus, startTime, duration);
        orderSessionStorage.save(orderSession);
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

//    private String getSessionKey(UUID orderSessionId) {
//        return "order:" + orderSessionId;
//    }

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

    public List<Order> loadOrders(
            Authentication authentication
    ) {
        if (authentication.isAuthenticated()) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getUserId();
            return orderRepository.findByUserId(userId);
        }
        throw new IllegalStateException();
    }

    public Page<Order> loadOrders(
            OrderSortType sortType,
            int pageNumber,
            int pageSize,
            Authentication authentication
    ) {
        if (authentication.isAuthenticated()) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getUserId();
            return orderRepository.findByUserId(userId, PageRequest.of(pageNumber, pageSize, sortType.getSort()));
        }
        throw new IllegalStateException();
    }
}
