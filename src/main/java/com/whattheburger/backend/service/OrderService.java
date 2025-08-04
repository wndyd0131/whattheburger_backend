package com.whattheburger.backend.service;

import com.whattheburger.backend.controller.dto.cart.CartResponseDto;
import com.whattheburger.backend.controller.dto.cart.ProductResponseDto;
import com.whattheburger.backend.controller.dto.order.OrderCreateRequestDto;
import com.whattheburger.backend.controller.dto.order.ProductOptionRequest;
import com.whattheburger.backend.controller.dto.order.ProductOptionTraitRequest;
import com.whattheburger.backend.controller.dto.order.ProductRequest;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.enums.OrderStatus;
import com.whattheburger.backend.domain.enums.OrderType;
import com.whattheburger.backend.domain.enums.PaymentStatus;
import com.whattheburger.backend.domain.order.*;
import com.whattheburger.backend.repository.*;
import com.whattheburger.backend.security.UserDetailsImpl;
import com.whattheburger.backend.service.dto.cart.ProcessedCartDto;
import com.whattheburger.backend.service.dto.cart.ProcessedProductDto;
import com.whattheburger.backend.service.exception.OrderNotFoundException;
import com.whattheburger.backend.service.exception.ProductNotFoundException;
import com.whattheburger.backend.service.exception.ProductOptionNotFoundException;
import com.whattheburger.backend.service.exception.ProductOptionTraitNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final CartService cartService;
    private final OrderStorage orderStorage;
    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductOptionTraitRepository productOptionTraitRepository;
    private final OrderProductRepository orderProductRepository;
    private final OrderProductOptionRepository orderProductOptionRepository;
    private final OrderProductOptionTraitRepository orderProductOptionTraitRepository;
    private final OrderFactory orderFactory;

    public Order createOrderPreview(UUID guestId, Authentication authentication, OrderType orderType) {
        ProcessedCartDto processedCartDto = cartService.loadCart(guestId.toString(), authentication);
        boolean isUser = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);

        if (isUser) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetailsImpl userDetails) {
                User user = userDetails.getUser();
                Order userOrderPreview = orderStorage.load(new UserKey(user.getId()))
                        .map(order -> orderFactory.overWriteOrder(processedCartDto, orderType, order))
                        .orElseGet(() -> {
                            Order orderPreview = orderFactory.createFromCartDto(processedCartDto, orderType);
                            orderPreview.changeUser(user);
                            return orderPreview;
                        });
                return orderStorage.save(userOrderPreview);
            }
            throw new IllegalStateException();
        } else {
            Order guestOrderPreview = orderStorage.load(new GuestKey(guestId))
                    .map(order -> orderFactory.overWriteOrder(processedCartDto, orderType, order))
                    .orElseGet(() -> {
                        Order orderPreview = orderFactory.createFromCartDto(processedCartDto, orderType);
                        orderPreview.changeGuestInfo(new GuestInfo(guestId));
                        return orderPreview;
                    });
            return orderStorage.save(guestOrderPreview);
        }
    }

    public Order loadOrderPreview(UUID guestId, Authentication authentication) {
        boolean isUser = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);

        if (isUser) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetailsImpl userDetails) {
                User user = userDetails.getUser();
                Order userOrderPreview = orderStorage.load(new UserKey(user.getId()))
                        .orElseThrow(() -> new OrderNotFoundException(user.getId()));
                return userOrderPreview;
            }
            throw new IllegalStateException();
        } else {
            if (guestId == null)
                throw new IllegalArgumentException();
            Order guestOrderPreview = orderStorage.load(new GuestKey(guestId))
                    .orElseThrow(() -> new OrderNotFoundException(guestId));
            return guestOrderPreview;
        }
    }

    private Map<Long, Product> createProductMap(List<ProductRequest> productRequests) {
        Set<Long> productIds = productRequests
                .stream()
                .map(ProductRequest::getProductId)
                .collect(Collectors.toSet());
        return productRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
    }
    private Map<Long, ProductOption> createProductOptionMap(List<ProductRequest> productRequests) {
        Set<Long> productOptionIds = new HashSet<>();
        for (ProductRequest productRequest : productRequests) {
            productRequest.getProductOptionRequests()
                    .stream()
                    .forEach(productOptionRequest -> productOptionIds.add(productOptionRequest.getProductOptionId()));
        }
        return productOptionRepository.findAllById(productOptionIds)
                .stream()
                .collect(Collectors.toMap(ProductOption::getId, Function.identity()));
    }
    private Map<Long, ProductOptionTrait> createProductOptionTraitMap(List<ProductRequest> productRequests) {
        Set<Long> productOptionTraitIds = new HashSet<>();
        for (ProductRequest productRequest : productRequests) {
            for (ProductOptionRequest productOptionRequest : productRequest.getProductOptionRequests()) {
                productOptionRequest.getProductOptionTraitRequests()
                        .stream()
                        .forEach(productOptionTraitRequest -> productOptionTraitIds.add(productOptionTraitRequest.getProductOptionTraitId()));
            }
        }
        return productOptionTraitRepository.findAllById(productOptionTraitIds)
                .stream()
                .collect(Collectors.toMap(ProductOptionTrait::getId, Function.identity()));
    }
}
