package com.whattheburger.backend.service;

import com.whattheburger.backend.controller.dto.OrderCreateRequestDto;
import com.whattheburger.backend.domain.Product;
import com.whattheburger.backend.domain.ProductOption;
import com.whattheburger.backend.domain.ProductOptionTrait;
import com.whattheburger.backend.repository.OrderRepository;
import com.whattheburger.backend.repository.ProductOptionRepository;
import com.whattheburger.backend.repository.ProductOptionTraitRepository;
import com.whattheburger.backend.repository.ProductRepository;
import com.whattheburger.backend.service.exception.ProductNotFoundException;
import com.whattheburger.backend.service.exception.ProductOptionNotFoundException;
import com.whattheburger.backend.service.exception.ProductOptionTraitNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.whattheburger.backend.controller.dto.OrderCreateRequestDto.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductOptionTraitRepository productOptionTraitRepository;

    @Transactional
    public void createOrder(OrderCreateRequestDto orderCreateRequestDto) {
        List<ProductRequest> productRequests = orderCreateRequestDto.getProductRequests();
        List<Product> products = new ArrayList<>();
        for (ProductRequest productRequest : productRequests) {
            Long productId = productRequest.getProductId();
            Product product = productRepository
                    .findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));
            for (OptionRequest optionRequest : productRequest.getOptionRequests()) {
                Long optionId = optionRequest.getOptionId();
                ProductOption productOption = productOptionRepository
                        .findByProductIdAndOptionId(productId, optionId)
                        .orElseThrow(() -> new ProductOptionNotFoundException(productId, optionId));
                Long productOptionId = productOption.getId();
                for (OptionTraitRequest optionTraitRequest : optionRequest.getOptionTraitRequests()) {
                    Long optionTraitId = optionTraitRequest.getOptionTraitId();
                    ProductOptionTrait optionTrait = productOptionTraitRepository
                            .findByProductOptionIdAndOptionTraitId(productOptionId, optionTraitId)
                            .orElseThrow(() -> new ProductOptionTraitNotFoundException(productOptionId, optionTraitId));
                }
            }
        }
    }
}
