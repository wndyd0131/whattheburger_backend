package com.whataburger.whataburgerproject.service;

import com.whataburger.whataburgerproject.controller.dto.OrderCreateRequestDto;
import com.whataburger.whataburgerproject.domain.Order;
import com.whataburger.whataburgerproject.domain.Product;
import com.whataburger.whataburgerproject.domain.ProductOption;
import com.whataburger.whataburgerproject.domain.ProductOptionTrait;
import com.whataburger.whataburgerproject.exception.ResourceNotFoundException;
import com.whataburger.whataburgerproject.repository.OrderRepository;
import com.whataburger.whataburgerproject.repository.ProductOptionRepository;
import com.whataburger.whataburgerproject.repository.ProductOptionTraitRepository;
import com.whataburger.whataburgerproject.repository.ProductRepository;
import com.whataburger.whataburgerproject.service.exception.OptionNotFoundException;
import com.whataburger.whataburgerproject.service.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.whataburger.whataburgerproject.controller.dto.OrderCreateRequestDto.*;

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
                        .orElseThrow(() -> new OptionNotFoundException(optionId));
                for (OptionTraitRequest optionTraitRequest : optionRequest.getOptionTraitRequests()) {
                    Long optionTraitId = optionTraitRequest.getOptionTraitId();
                    ProductOptionTrait optionTrait = productOptionTraitRepository
                            .findByProductOptionIdAndOptionTraitId(productId, optionTraitId)
                            .orElseThrow(() -> new ResourceNotFoundException("OptionTrait", optionTraitId, HttpStatus.NOT_FOUND));
                }
            }
        }
    }
}
