package com.whattheburger.backend.service;

import com.whattheburger.backend.domain.ProductOption;
import com.whattheburger.backend.repository.ProductOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductOptionService {

    private final ProductOptionRepository productOptionRepository;

    public List<ProductOption> getProductOptionByProductId(Long productId) {
        List<ProductOption> productOptions = productOptionRepository.findByProductId(productId);
        return productOptions;
    }
}
