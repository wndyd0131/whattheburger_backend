package com.whataburger.whataburgerproject.service;

import com.whataburger.whataburgerproject.domain.ProductOption;
import com.whataburger.whataburgerproject.repository.ProductOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductOptionService {

    private final ProductOptionRepository productOptionRepository;

    public List<ProductOption> findProductOptionByProductId(Long productId) {
        List<ProductOption> productOptions = productOptionRepository.findByProductId(productId);
        return productOptions;
    }
}
