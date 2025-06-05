package com.whattheburger.backend.service;

import com.whattheburger.backend.controller.dto.OrderCreateRequestDto;
import com.whattheburger.backend.controller.dto.ProductCreateRequestDto;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.repository.*;
import com.whattheburger.backend.service.exception.ProductNotFoundException;
import com.whattheburger.backend.service.exception.ProductOptionNotFoundException;
import com.whattheburger.backend.service.exception.ProductOptionTraitNotFoundException;
import com.whattheburger.backend.service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.whattheburger.backend.controller.dto.OrderCreateRequestDto.*;

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
                .orderType(orderCreateRequestDto.getOrderType())
                .orderNote(orderCreateRequestDto.getOrderNote())
                .paymentMethod(orderCreateRequestDto.getPaymentMethod())
                .totalPrice(orderCreateRequestDto.getTotalPrice())
                .discountPrice(orderCreateRequestDto.getDiscountPrice())
                .couponApplied(orderCreateRequestDto.getCouponApplied())
                .build();

        Order newOrder = orderRepository.save(order);

        List<ProductRequest> productRequests = orderCreateRequestDto.getProductRequests();

        Set<Long> productIds = orderCreateRequestDto.getProductRequests().stream()
                .map(ProductRequest::getProductId)
                .collect(Collectors.toSet());
        Map<Long, Product> productMap = productRepository.findAllById(productIds).stream().collect(Collectors.toMap(Product::getId, Function.identity()));

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

            Set<Long> productOptionIds = productRequest.getProductOptionRequests().stream()
                    .map(ProductOptionRequest::getProductOptionId)
                    .collect(Collectors.toSet());

            Map<Long, ProductOption> productOptionMap = productOptionRepository.findAllById(productOptionIds).stream()
                    .collect(Collectors.toMap(ProductOption::getId, Function.identity()));

            for (OrderCreateRequestDto.ProductOptionRequest productOptionRequest : productRequest.getProductOptionRequests()) {
                Long productOptionId = productOptionRequest.getProductOptionId();
                ProductOption productOption = Optional.ofNullable(productOptionMap.get(productOptionId))
                        .orElseThrow(() -> new ProductOptionNotFoundException(productOptionId));
                OrderProductOption orderProductOption = new OrderProductOption(
                        productOptionRequest.getQuantity(),
                        orderProduct,
                        productOption
                );
                orderProductOptionRepository.save(orderProductOption);

                Set<Long> productOptionTraitIds = productOptionRequest.getProductOptionTraitRequests().stream()
                        .map(ProductOptionTraitRequest::getProductOptionTraitId)
                        .collect(Collectors.toSet());

                Map<Long, ProductOptionTrait> productOptionTraitMap = productOptionTraitRepository.findAllById(productOptionTraitIds).stream()
                        .collect(Collectors.toMap(ProductOptionTrait::getId, Function.identity()));

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
}
