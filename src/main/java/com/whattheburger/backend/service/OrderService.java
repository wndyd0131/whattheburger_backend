package com.whattheburger.backend.service;

import com.whattheburger.backend.controller.dto.order.OrderCreateRequestDto;
import com.whattheburger.backend.controller.dto.order.ProductOptionRequest;
import com.whattheburger.backend.controller.dto.order.ProductOptionTraitRequest;
import com.whattheburger.backend.controller.dto.order.ProductRequest;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.enums.OrderStatus;
import com.whattheburger.backend.repository.*;
import com.whattheburger.backend.service.exception.ProductNotFoundException;
import com.whattheburger.backend.service.exception.ProductOptionNotFoundException;
import com.whattheburger.backend.service.exception.ProductOptionTraitNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.whattheburger.backend.controller.dto.order.OrderCreateRequestDto.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductOptionTraitRepository productOptionTraitRepository;
    private final OrderProductRepository orderProductRepository;
    private final OrderProductOptionRepository orderProductOptionRepository;
    private final OrderProductOptionTraitRepository orderProductOptionTraitRepository;

    @Transactional
    public Order createOrder(OrderCreateRequestDto orderCreateRequestDto) {
        Order order = Order
                .builder()
                .orderStatus(OrderStatus.IN_PROCESS)
                .orderType(orderCreateRequestDto.getOrderType())
                .orderNote(orderCreateRequestDto.getOrderNote())
                .paymentMethod(orderCreateRequestDto.getPaymentMethod())
                .totalPrice(orderCreateRequestDto.getTotalPrice())
                // coupon
                // store
                .build();

        Order newOrder = orderRepository.save(order);

        List<ProductRequest> productRequests = orderCreateRequestDto.getProductRequests();

        Map<Long, Product> productMap = createProductMap(productRequests);
        Map<Long, ProductOption> productOptionMap = createProductOptionMap(productRequests);
        Map<Long, ProductOptionTrait> productOptionTraitMap = createProductOptionTraitMap(productRequests);

        for (ProductRequest productRequest : productRequests) {
            Product product = Optional.ofNullable(productMap.get(productRequest.getProductId()))
                    .orElseThrow(() -> new ProductNotFoundException(productRequest.getProductId()));

            OrderProduct orderProduct = OrderProduct
                    .builder()
                    .quantity(productRequest.getQuantity())
                    .forWhom(productRequest.getForWhom())
                    .order(order)
                    .product(product)
                    .build();
            orderProductRepository.save(orderProduct);

            for (ProductOptionRequest productOptionRequest : productRequest.getProductOptionRequests()) {
                Long productOptionId = productOptionRequest.getProductOptionId();
                ProductOption productOption = Optional.ofNullable(productOptionMap.get(productOptionId))
                        .orElseThrow(() -> new ProductOptionNotFoundException(productOptionId));
                OrderProductOption orderProductOption = new OrderProductOption(
                        productOptionRequest.getQuantity(),
                        orderProduct,
                        productOption
                );
                orderProductOptionRepository.save(orderProductOption);

                for (ProductOptionTraitRequest productOptionTraitRequest : productOptionRequest.getProductOptionTraitRequests()) {
                    Long productOptionTraitId = productOptionTraitRequest.getProductOptionTraitId();
                    ProductOptionTrait productOptionTrait = Optional.ofNullable(productOptionTraitMap.get(productOptionTraitId))
                            .orElseThrow(() -> new ProductOptionTraitNotFoundException(productOptionTraitId));

                    OrderProductOptionTrait orderProductOptionTrait = new OrderProductOptionTrait(
                            productOptionTraitRequest.getValue(),
                            orderProductOption,
                            productOptionTrait
                    );
                    orderProductOptionTraitRepository.save(orderProductOptionTrait);
                }
            }
        }
        return newOrder;
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
